import akka.actor.AbstractActor;
import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.PatternsCS;
import changes.SectionUpdate;
import changes.ViewUpdate;
import changes.ViewUpdateBuilder;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import games.GameDefinition;
import messages.*;
import messages.SectionReply.Snapshot;
import world.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;


public class ViewActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    private final GameDefinition gameDefinition;
    private final ViewDefinition viewDefinition;
    private final WorldDefinition worldDefinition;
    private final ViewVersion viewVersion;
    private final View view;
    private final ViewUpdateBuilder viewBuilder;
    private final ActorRef mediator;

    private Map<Integer, ActorRef> sectionIdToActor;

    private final Map<Integer, ViewBorder> viewBorders;

    public ViewActor(ViewDefinition viewDefinition, GameDefinition gameDefinition) {
        this.gameDefinition = gameDefinition;
        this.viewDefinition = viewDefinition;
        this.worldDefinition = gameDefinition.worldDefinition();
        this.viewVersion = ViewVersion.initial(viewDefinition.mainSection);
        this.view = new View(worldDefinition, viewDefinition);

        this.viewBuilder = new ViewUpdateBuilder(view);
        this.viewBorders = createBorders();

        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
    }

    public static Props props(ViewDefinition viewDefinition, GameDefinition gameDefinition) {
        return Props.create(ViewActor.class, viewDefinition, gameDefinition);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientCommand.AddUnit.class, this::process)
                .match(SectionBroadcastMessage.UnitAdded.class, this::process)
                .match(SectionBroadcastMessage.UnitRemoved.class, this::process)
                .match(SectionBroadcastMessage.CombatResult.class, this::process)
                .build();
    }

    @Override
    public void preStart() {
        log.info("ViewActor preStart, looking-up SectionActors");


        Function<Integer, ActorPath> path = (Integer sectionNo) -> getContext().getParent().path().parent().child(ActorNames.sectionName(sectionNo));
        var actorRef = path.andThen(ap -> getContext().actorFor(ap));

        var sectionsInView = view.masterSectionsInView();

        sectionIdToActor = sectionsInView
                .stream()
                .map(s -> {
                    log.info("Path to section:[{}] is [{}]", s, path.apply(s));
                    return s;
                })
                .collect(
                        toUnmodifiableMap(sectionNo -> sectionNo, actorRef)
                );


        List<Snapshot> all = Lists.newCopyOnWriteArrayList();
        var start = CompletableFuture.completedStage(all);


        BiFunction<List<Snapshot>, Object, List<Snapshot>> listCombine = (list, o) -> {
            log.info("Got reply " + o);
            list.add((Snapshot) o);
            return list;
        };

        Executor executor = getContext().dispatcher();

        for (var e : sectionIdToActor.entrySet()) {
            int sectionNo = e.getKey();
            this.mediator.tell(new DistributedPubSubMediator.Subscribe(BusTopics.sectorBroadcast(sectionNo), self()), self());
            this.mediator.tell(new DistributedPubSubMediator.Subscribe(BusTopics.sectorCursorBroadcast(sectionNo), self()), self());

            var cs = PatternsCS.ask(e.getValue(), new SectionMessage.RequestSnapshot(), gameDefinition.standardTimeout());
            start = start.thenCombineAsync(cs, listCombine);
        }
        //When every one has replied combine the snapshots into a new state in new coordinate system
        start.thenApply(this::combineSnapshots)
                .whenCompleteAsync((state, t) -> {
                    if (t != null) {
                        log.error(t, "Failed to gather snapshots");
                        toSession(new SessionMessage.SessionTerminate(t));
                    } else {
                        log.info("Sending View Snapshot:[{}]", state);
                        toSession(state);
                    }
                }, executor);


        toSession(new ViewMessage.InitView(this.viewDefinition, sectionsInView, viewBorders));

        //ToDo we should start listening to these earlier and buffer them up until we have the snapshots
        //Once all snapshots are in place we can apply the deltas based on the section version
        log.info("Starting to listen to Section Broadcasts to propagate any deltas");
        for (var sectionNo : sectionsInView) {
            this.mediator.tell(new DistributedPubSubMediator.Subscribe(BusTopics.sectorBroadcast(sectionNo), self()), self());
        }

    }

    @Override
    public void postStop() {
        this.sectionIdToActor.keySet().forEach(section -> {
            this.mediator.tell(new DistributedPubSubMediator.Remove(BusTopics.sectorBroadcast(section)), self());
            this.mediator.tell(new DistributedPubSubMediator.Remove(BusTopics.sectorCursorBroadcast(section)), self());
        });
    }

    private ViewUpdate.Snapshot combineSnapshots(List<SectionReply.Snapshot> snapshotReplies) {
        final ViewUpdateBuilder vb = new ViewUpdateBuilder(view);
        Instant latest = Instant.EPOCH;
        for (var sr : snapshotReplies) {
            var sectionSnapshot = sr.snapshot;
            if (sectionSnapshot.sectionVersion.time.isAfter(latest)) latest = sectionSnapshot.sectionVersion.time;
            vb.addSectionSnapshot(sectionSnapshot);
        }

        return vb.buildSnapshot(viewVersion.next());

    }

    private Map<Integer, ViewBorder> createBorders() {
        var sw = Stopwatch.createStarted();
        var result = this.view.masterSectionsInView().stream().collect(toUnmodifiableMap(s -> s, s -> ViewBorder.create(view, s)));
        log.info("Took [{}] to create borders", sw.elapsed());
        return result;
    }


    private void process(ClientCommand.AddUnit addUnit) {
        log.error("Not yet implemented");
    }

    /**
     * On receive of the Combat Result on a section we publish out the transformed delta to the session and then the actual stats
     *
     * @param result
     */
    private void process(SectionBroadcastMessage.CombatResult result) {
        log.debug("Received CombatResult [{}] forwarding to sink ", result);
        viewBuilder.clear();
        viewBuilder.addSectionDelta((SectionUpdate.Delta) result.combatResult.delta);
        var viewDelta = viewBuilder.buildDelta(viewVersion.next());
        toSession(viewDelta);

        toSession(result.combatResult.combatStats);

    }

    private void process(SectionBroadcastMessage.UnitAdded unitAdded) {
        int section = unitAdded.sectionNo;
        var worldCord = view.sectionToView(section).apply(unitAdded.cord);
        var unit = unitAdded.unit;
        var msg = new ClientMessage.AddUnit(worldCord, unit);
        toSession(msg);

    }

    private void process(SectionBroadcastMessage.UnitRemoved unitRemoved) {
        toSession(unitRemoved);
    }

    private void toSession(Object msg) {
        this.getContext().getParent().tell(msg, self());
    }
}
