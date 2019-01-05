import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Status;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import games.GameDefinition;
import messages.*;
import world.*;
import world.exceptions.AddUnitException;

import java.util.Map;

public class SectionActor extends AbstractActorWithTimers {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<Player, ActorRef> players = Maps.newHashMap();
    private final Section section;
    private final int sectorNo;
    private final GameDefinition gameDefinition;

    private final Receive normal;
    private final Receive combat;

    private SectionNeighbours neighbours;

    private final Object COMBAT_KEY = "CombatKey";

    private final ActorRef broadcaster;

    private int combatCounter = 1;


    public static Props props(Section section, GameDefinition gameDefinition) {
        return Props.create(SectionActor.class, section, gameDefinition);
    }

    private SectionActor(Section section, GameDefinition gameDefinition) {

        this.section = section;
        this.sectorNo = section.sectionNo;
        this.gameDefinition = gameDefinition;
        this.normal = createReceiveNormal();
        this.combat = createReceiveCombat();

        var firstDuration = gameDefinition.combatCycle().multipliedBy(sectorNo + 1);

        getTimers().startSingleTimer(
                COMBAT_KEY,
                new SectionMessage.StartCombat(new SectorTotalCombatStats()),
                firstDuration);

        var zoneOfControl = gameDefinition.zoneOfControlCycle();
        Object ZONE_OF_CONTROL_KEY = "ZoneOfControl";
        getTimers().startPeriodicTimer(
                ZONE_OF_CONTROL_KEY,
                new SectionMessage.CalculateZoneOfControl(),
                zoneOfControl);


        this.broadcaster = DistributedPubSub.get(getContext().system()).mediator();
    }

    @Override
    public void preStart() {
        log.debug("SectionActor preStart, creating SectionCursorsActor");
        getContext().actorOf(SectionCursorsActor.props(gameDefinition, this.sectorNo), SectionCursorsActor.ActorName);
    }


    @Override
    public Receive createReceive() {
        return this.normal;
    }

    private Receive createReceiveCombat() {
        return receiveBuilder()
                .match(SectionNeighbours.class, this::process)
                .match(SectionMessage.SlaveSetUnit.class, this::process)
                .match(SectionMessage.RegisterPlayer.class, this::process)
                .match(SectionMessage.UnregisterPlayer.class, this::process)
                .match(SectionMessage.TryAddUnit.class, this::processWhenInCombat)
                .match(SectionMessage.StopCombat.class, this::process)
                .match(SectionMessage.CalculateZoneOfControl.class, this::ignoreCombat)
                .matchAny(o -> log.warning("received unknown message [{}] when in combat state", o))
                .build();
    }

    private void ignoreCombat(Object message) {
        log.debug("Ignoring [{}] while in combat state", message);
    }


    private Receive createReceiveNormal() {
        return receiveBuilder()
                .match(SectionMessage.RegisterPlayer.class, this::process)
                .match(SectionMessage.UnregisterPlayer.class, this::process)
                .match(SectionMessage.TryAddUnit.class, this::process)
                .match(SectionMessage.TryRemoveUnit.class, this::process)
                .match(SectionMessage.SlaveSetUnit.class, this::process)
                .match(SectionMessage.SlaveRemoveUnit.class, this::process)
                .match(SectionNeighbours.class, this::process)
                .match(SectionMessage.StartCombat.class, this::process)
                .match(SectionMessage.RequestSnapshot.class, this::process)
                .match(SectionMessage.CalculateZoneOfControl.class, this::process)
                .matchAny(o -> log.warning("received unknown message [{}]", o))
                .build();
    }



    private void process(SectionMessage.CalculateZoneOfControl calculateZoneOfControl) {
        /*
        CalculationInfo calcInfo = CalculationInfo.start();
        Stream<CellControl> zoneOfControlResult = section.calculateZoneOfControl();
        calcInfo.stop();
        */

        //log.debug("Broadcasting ZoneOfControl message");
        //Inform all sessions

        var zocSnap = this.section.calculateZoneOfControl();

        this.broadcastZoneOfControl(zocSnap);

    }


    @SuppressWarnings("unused")
    private void process(SectionMessage.RequestSnapshot requestSnapshot) {
        log.info("[{}] requested snapshot", sender().path());
        var state = section.snapshot();
        sender().tell(new SectionReply.Snapshot(state), self());
    }


    private void process(SectionMessage.RegisterPlayer registerPlayer) {
        var playerInfo = registerPlayer.playerInfo;

        players.put(playerInfo.player, playerInfo.playerActor);
        log.info("Registered Player:[{}]", playerInfo);
        var sectorInfo = new SectionInfo(self(), sectorNo);
        sender().tell(new SectionMessage.RegisterPlayer.Ack(sectorInfo), self());
    }

    private void process(SectionMessage.UnregisterPlayer unregisterPlayer) {
        var player = unregisterPlayer.player;
        assert players.containsKey(player);
        if (!players.containsKey(player)) {
            log.warning("Received unregister playerActor:[{}] but playerActor was not in map. Had players [{}]", player, players.keySet());
        }
        players.remove(player);

        var sectorInfo = new SectionInfo(self(), sectorNo);
        sender().tell(new SectionMessage.UnregisterPlayer.Ack(sectorInfo), self());
    }


    private void process(SectionMessage.TryAddUnit addUnit) {
        ActorRef originator = addUnit.originator;
        try {
            int cordMasterSection = section.getMasterForCord(addUnit.cord);
            if (cordMasterSection == this.sectorNo) {

                section.tryAddUnitAtCord(addUnit.unit, addUnit.cord);
                log.info("Added unit [{}] at [{}]", addUnit.unit, addUnit.cord);
                //Tell all slaves about the update
                section.slavesAtCord(addUnit.cord).forEach((sectionNo, cord) -> {
                    log.info("Informing slave [{}]", sectionNo);
                    var slaveCord = section.translate(addUnit.cord, sectionNo);
                    neighbours.slaves.get(sectionNo).tell(new SectionMessage.SlaveSetUnit(addUnit.unit, slaveCord), self());
                });
                sender().tell(new SectionMessage.Ok(sectorNo), self());
                broadcastUnitAdded(addUnit.unit, addUnit.cord);
            } else {
                //Forward request to master
                var masterCord = section.translate(addUnit.cord, cordMasterSection);
                var toMaster = new SectionMessage.TryAddUnit(addUnit.unit, masterCord, originator);
                log.info("Forward add at [{}]->[{}] to master [{}]", addUnit.cord, toMaster.cord, cordMasterSection);
                neighbours.masters.get(cordMasterSection).forward(toMaster, getContext());
            }


        } catch (AddUnitException e) {
            sender().tell(new Status.Failure(e), self());
        } catch (RuntimeException re) {
            log.error("Unexpected failure to add unit at:[{}] error:[{}]", addUnit.cord, re.getMessage());
            sender().tell(new Status.Failure(re), self());//This result in exception on the other side
        }
    }

    private void process(SectionMessage.TryRemoveUnit message) {
        try {
            int cordMasterSection = section.getMasterForCord(message.cord);
            var cord = message.cord;
            var team = message.team;
            if (cordMasterSection == this.sectorNo) {

                var unit = section.tryRemoveUnitAtCord(message.team, message.cord);
                log.info("Removed unit for team [{}] at [{}]", message.team, message.cord);
                //Tell all slaves about the update
                section.slavesAtCord(message.cord).forEach((sectionNo, cordInSelf) -> {
                    log.info("Informing slave [{}]", sectionNo);
                    neighbours.slaves.get(sectionNo).tell(new SectionMessage.SlaveRemoveUnit(unit, cord), self());
                });
                sender().tell(new SectionMessage.Ok(sectorNo), self());
                broadcastUnitRemoved(unit, message.cord);

                sender().tell(new SectionMessage.TryRemoveUnit.Ack(unit, cord, team), self());
            } else {
                //Forward request to master
                var toMaster = new SectionMessage.TryRemoveUnit(team, section.translate(message.cord, cordMasterSection));
                log.info("Forward add at [{}]->[{}] to master [{}]", message.cord, toMaster.cord, cordMasterSection);
                neighbours.masters.get(cordMasterSection).forward(toMaster, getContext());
            }


        } catch (RuntimeException re) {
            log.info("Failed to add unit at:[{}] error:[{}]", message.cord, re.getMessage());
            sender().tell(new Status.Failure(re), self());
        }
    }

    @SuppressWarnings("unused")
    private void processWhenInCombat(SectionMessage.TryAddUnit addUnit) {
        sender().tell(new Status.Failure(new IllegalStateException("Can't add unit while in combat state")), self());
    }

    private void process(SectionMessage.SlaveSetUnit addUnit) {
        log.info("Section informed by master to set unit [{}] at [{}]", addUnit.unit, addUnit.cord);
        if (section.isOccupied(addUnit.cord)) {
            log.warning("Cord [{}] already occupied???");
        }
        section.slaveAddUnitAtCord(addUnit.unit, addUnit.cord);
        broadcastUnitAdded(addUnit.unit, addUnit.cord);
    }

    private void process(SectionMessage.SlaveRemoveUnit message) {
        log.info("Section informed by master to remove unit [{}] at [{}]", message.unit, message.cord);
        if (!section.isOccupied(message.cord)) {
            log.warning("Cord is empty ???");
            return;
        }
        section.slaveRemoveUnitAtCord(message.unit, message.cord);
    }


    private void process(SectionNeighbours sn) {
        neighbours = sn;
    }

    @SuppressWarnings("unused")
    private void process(SectionMessage.StartCombat start) {
        log.info("Starting Combat");
        this.getContext().become(combat);

        broadcast(new SectionBroadcastMessage.CombatStarted(sectorNo, combatCounter));

        var result = this.section.combat();


        var combatResult = new SectionBroadcastMessage.CombatResult(sectorNo, combatCounter, result);
        //Inform all session about the result
        broadcast(combatResult);

        //Inform players in the mainSection, about number of units allocated to their team
        var newUnits = section.newUnitAllocation(players.keySet().stream());

        var baseUnits = section.baseUnitAllocation(players.keySet().stream());

        var playersWithPoints = Sets.union(newUnits.keySet(), baseUnits.keySet());


        log.debug("New Unit Distribution [{}]", newUnits);
        playersWithPoints.forEach((player) -> {
                    Map<PlayerMessage.AllocationReason, Integer> map = Maps.newEnumMap(PlayerMessage.AllocationReason.class);
                    var sector = newUnits.get(player);
                    if (sector != null) map.put(PlayerMessage.AllocationReason.Sector, sector);

                    var base = baseUnits.get(player);
                    if (base != null) map.put(PlayerMessage.AllocationReason.Base, base);

                    var msg = new PlayerMessage.SectionAllocatedUnits(this.sectorNo, map);
                    players.get(player).tell(msg, self());
                }
        );


        getTimers().startSingleTimer(
                COMBAT_KEY,
                new SectionMessage.StartCombat(new SectorTotalCombatStats()),
                gameDefinition.combatCycle());
        this.self().tell(new SectionMessage.StopCombat(new SectorCombatStats()), self());

        broadcast(new SectionBroadcastMessage.CombatEnded(sectorNo, combatCounter));
        combatCounter++;
    }

    private void process(@SuppressWarnings("unused") SectionMessage.StopCombat stop) {
        log.info("Stopping Combat");
        this.getContext().become(normal);
    }

    private void broadcastUnitAdded(Unit unit, Cord cord) {
        log.debug("Broadcasting unit [{}] added to cord:[{}]", unit, cord);
        broadcast(new SectionBroadcastMessage.UnitAdded(sectorNo, unit, cord));
    }

    private void broadcastUnitRemoved(Unit unit, Cord cord) {
        log.debug("Broadcasting unit [{}] removed from cord:[{}]", unit, cord);
        broadcast(new SectionBroadcastMessage.UnitRemoved(sectorNo, unit, cord));
    }


    private boolean lastZoneOfControlEmpty = false;

    private void broadcastZoneOfControl(ZocResult result) {

        if (result.isEmpty()) {
            if (!result.isSnapshot()) return;
            if (lastZoneOfControlEmpty) return;
        }
        broadcast(new SectionBroadcastMessage.ZoneOfControl(this.sectorNo, result.gained, result.lost));
        lastZoneOfControlEmpty = result.isEmpty();
    }

    private void broadcast(SectionBroadcastMessage message) {
        var topic = BusTopics.sectorBroadcast(this.sectorNo);
        if (!(message instanceof SectionBroadcastMessage.ZoneOfControl))
            log.debug("Broadcasting to topic [{}] [{}]", topic, message);
        broadcaster.tell(new DistributedPubSubMediator.Publish(topic, message), self());
    }
}

