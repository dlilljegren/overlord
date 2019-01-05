import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.Maps;
import games.GameDefinition;
import messages.CursorMessage;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SectionCursorsActor extends AbstractActorWithTimers {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final GameDefinition gameDefinition;
    private final Integer sectionNo;

    final static String ActorName = "cursors";

    final static String CursorBroadcast = "CursorBroadcastTimer";

    private final ActorRef mediator;

    private final Map<ActorRef, CursorMessage.MoveCursor> lastCursors;
    private final Map<ActorRef, UUID> sessionToId;

    private boolean isDirty = true;

    public static Props props(GameDefinition gameDefinition, Integer sectionNo) {
        return Props.create(SectionCursorsActor.class, gameDefinition, sectionNo);
    }

    SectionCursorsActor(GameDefinition gameDefinition, Integer sectionNo) {

        this.gameDefinition = gameDefinition;
        this.sectionNo = sectionNo;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        this.lastCursors = Maps.newHashMap();
        this.sessionToId = Maps.newHashMap();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BroadcastState.class, this::process)
                .match(CursorMessage.MoveCursor.class, this::process)
                .match(CursorMessage.RemoveSession.class, this::process)
                .match(Terminated.class, this::process)
                .matchAny(o -> log.info("received unknown message [{}]", o))
                .build();
    }


    private void process(BroadcastState broadcastState) {
        if (isDirty) {
            CursorMessage.CursorState msg = createState();
            mediator.tell(new DistributedPubSubMediator.Publish(BusTopics.sectorCursorBroadcast(this.sectionNo), msg),
                    getSelf());
            isDirty = false;
        }
    }

    private CursorMessage.CursorState createState() {
        final Map<UUID, CursorMessage.MoveCursor> cursorMap = sessionToId.entrySet().stream().collect(Collectors.toMap(e -> e.getValue(), e -> lastCursors.get(e.getKey())));
        return new CursorMessage.CursorState(sectionNo, cursorMap);
    }

    private void process(Terminated terminated) {
        self().tell(new CursorMessage.RemoveSession(terminated.actor()), self());
    }

    private void process(CursorMessage.RemoveSession removeSession) {
        context().unwatch(removeSession.session);
        lastCursors.remove(removeSession.session);
        sessionToId.remove(removeSession.session);
        if (lastCursors.isEmpty()) timers().cancel(CursorBroadcast);
        log.debug("lost session actor [{}]", removeSession.session.path());
        isDirty = true;
    }

    private void process(CursorMessage.MoveCursor msg) {

        assert msg != null;
        assert msg.session != null;
        //check if we have in cache
        if (!lastCursors.containsKey(msg.session)) {
            getContext().watch(msg.session);
            sessionToId.put(msg.session, UUID.randomUUID());
        }
        lastCursors.put(msg.session, msg);
        if (lastCursors.size() == 1) {
            timers().startPeriodicTimer(CursorBroadcast, new BroadcastState(), gameDefinition.cursorBroadcastCycle());
        }
        isDirty = true;
    }


    private static class BroadcastState {

    }
}
