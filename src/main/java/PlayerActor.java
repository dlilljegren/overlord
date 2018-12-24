import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.PatternsCS;
import games.GameDefinition;
import messages.*;
import world.AddUnitException;
import world.Player;
import world.UnitBank;

public class PlayerActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final Player player;
    private final PlayerInfo playerInfo;

    private final UnitBank unitBank;
    private final GameDefinition gameDefinition;

    private final Receive waitForSession;
    private final Receive waitForSessionAck;
    private final Receive waitForSector;
    private final Receive waitForSectorAck;
    private final Receive playing;


    private final ActorLookup actorLookup;

    private final SessionState sessionState;
    private final SectionState sectionState;

    public static Props props(GameDefinition gameDefinition, Player player) {
        return Props.create(PlayerActor.class, gameDefinition, player);
    }

    PlayerActor(GameDefinition gameDefinition, Player player) {

        this.player = player;
        this.unitBank = new UnitBank();
        this.unitBank.addUnits(20);
        this.gameDefinition = gameDefinition;
        this.playerInfo = new PlayerInfo(self(), player);

        this.sessionState = new SessionState();
        this.sectionState = new SectionState();

        this.waitForSession = createWaitForSession();
        this.waitForSessionAck = createWaitForSessionAck();

        this.waitForSector = createWaitForSection();
        this.waitForSectorAck = createWaitForSectorAck();

        this.playing = createPlaying();

        this.actorLookup = new ActorLookup(getContext());
    }

    private Receive createWaitForSession() {
        return receiveBuilder()
                .match(GameMessage.AssignSession.class, this::processWaitSession)
                .match(SessionTerminated.class, this::processAllStates)
                .match(SectionMessage.UnregisterPlayer.Ack.class, this::processAllStates)
                .matchAny(o -> log.info("received unknown message [{}] while in WaitForSession state", o))
                .build();
    }

    private Receive createWaitForSessionAck() {
        return receiveBuilder()
                .match(SessionMessage.RegisterPlayer.Ack.class, this::processWaitSessionAck)
                .match(PlayerCommand.AssignSection.class, this::processWaitSessionAck)
                .match(SectionMessage.RegisterPlayer.Ack.class, this::processWaitSectorAck)
                .match(SessionTerminated.class, this::processAllStates)
                .match(SectionMessage.UnregisterPlayer.Ack.class, this::processAllStates)
                .matchAny(o -> log.info("received unknown message [{}] while in WaitForSessionAck state", o))
                .build();
    }


    private Receive createWaitForSection() {
        return receiveBuilder()
                .match(PlayerCommand.AssignSection.class, this::processWaitSector)
                .match(SessionTerminated.class, this::processAllStates)
                .match(SectionMessage.UnregisterPlayer.Ack.class, this::processAllStates)
                .matchAny(o -> log.info("received unknown message [{}] while in WaitForSector state", o))
                .build();
    }

    private Receive createWaitForSectorAck() {
        return receiveBuilder()
                .match(SectionMessage.RegisterPlayer.Ack.class, this::processWaitSectorAck)
                .match(SessionMessage.RegisterPlayer.Ack.class, this::processWaitSectorAck)
                .match(SessionTerminated.class, this::processAllStates)
                .match(SectionMessage.UnregisterPlayer.Ack.class, this::processAllStates)
                .matchAny(o -> log.info("received unknown message [{}] while in WaitForSectorAck state", o))
                .build();
    }

    private Receive createPlaying() {
        return receiveBuilder()
                .match(PlayerCommand.AddUnit.class, this::process)
                .match(ClientCommand.RemoveUnit.class, this::process)
                .match(PlayerMessage.SectionAllocatedUnits.class, this::process)
                .match(GameMessage.AssignSession.class, this::process)
                .match(SectionMessage.TryRemoveUnit.Ack.class, this::process)
                .match(SessionTerminated.class, this::processAllStates)
                .match(SectionMessage.UnregisterPlayer.Ack.class, this::processAllStates)
                .match(PlayerCommand.AssignSection.class, this::processPlaying)
                .matchAny(o -> log.info("received unknown message [{}] while in Playing state", o))
                .build();
    }

    private void process(SectionMessage.TryRemoveUnit.Ack message) {
        unitBank.addUnits(1);
        sessionState.tell(new PlayerMessage.UnitStatus(unitBank.balance()));
    }

    private void process(PlayerMessage.SectionAllocatedUnits message) {
        unitBank.addUnits(message.totalUnits());
        sessionState.tell(new PlayerMessage.UnitStatus(unitBank.balance()));
    }

    private void process(final PlayerCommand.AddUnit message) {
        assert sessionState.hasSession();
        assert sectionState.hasSection();
        try {
            log.info("AddUnit: [{}]", message);
            SectionMessage.TryAddUnit sectionMessage = new SectionMessage.TryAddUnit(player.createUnit(), message.cord, self());
            if (unitBank.hasUnits()) {
                unitBank.addUnits(-1);
                var sectionActor = this.actorLookup.findSection(message.section);

                var sender = sender();
                PatternsCS.ask(sectionActor, sectionMessage, gameDefinition.standardTimeout())
                        .thenAcceptAsync((v) -> sessionState.tell(new PlayerMessage.UnitStatus(unitBank.balance())), getContext().dispatcher())
                        .exceptionally(t -> {
                            unitBank.addUnits(1);//ToDo need to either make unitBank thread safe of make sure it's only accessed in single thread
                            if (t.getCause() instanceof AddUnitException) {
                                sender.tell(new PlayerCommand.AddUnit.Failed(t.getCause().getMessage(), message.section, message.cord), self());
                            } else {
                                log.error(t, "Player failed to add unit [{}] error:[{}] reply:[{}]", sectionMessage, t.getMessage());
                                sender.tell(new Status.Failure(t), self());//ToDo need to send specific failure messages
                            }

                            return null;
                        });


            } else {
                log.info("No units left");
                sessionState.tell(new ClientMessage.InfoMsg("No units left"));
            }

        } catch (RuntimeException re) {
            sender().tell(new Status.Failure(re), self());
        }
    }

    /**
     * Client want to take back a previously placed unit
     *
     * @param removeUnit unit to remove
     */
    private void process(ClientCommand.RemoveUnit removeUnit) {
        assert sectionState.hasSection();
        try {
            SectionMessage.TryRemoveUnit message = new SectionMessage.TryRemoveUnit(player.team, removeUnit.cord);
            sectionState.tell(message);

        } catch (RuntimeException re) {
            sender().tell(new Status.Failure(re), self());
        }
    }

    private void process(GameMessage.AssignSession assignSession) {
        ActorRef newSession = assignSession.session;
        if (newSession.equals(sessionState.sessionActor)) {
            log.warning("Got [{}] while in state playing, session was already assigned will ignore", assignSession);
            return;
        }
        log.debug("Assigned to new session [{}] while in state playing, will switch to wait for session and re-send message", newSession);
        sessionState.unassignSession();

        getContext().become(waitForSession);

        self().forward(assignSession, getContext());
    }

    private void processWaitSession(GameMessage.AssignSession message) {
        assert !sessionState.hasSession();


        //Send registration to session
        log.info("Registering player to session [{}]", message.session);
        var playerInfo = new PlayerInfo(this.self(), player);
        sessionState.setWaitingForRegistration();
        message.session.tell(new SessionMessage.RegisterPlayer(playerInfo), self());


        getContext().become(waitForSessionAck);
    }


    /**
     * What to do in case we get assigned to a sector before we have a session to tell about it
     *
     * @param message incoming
     */
    private void processWaitSessionAck(PlayerCommand.AssignSection message) {
        log.debug("Got assign to sector [{}] while still waiting for session ack, will still go ahead and register at sector", message.sectionNo);

        process(message);

        //Stay in same state until we get the ack
    }

    private void processWaitSector(PlayerCommand.AssignSection message) {
        assert sessionState.hasSession();
        log.debug("Player registering to Section {[]} register at sector", message.sectionNo);
        process(message);
        getContext().become(waitForSectorAck);
    }


    private void processPlaying(PlayerCommand.AssignSection message) {
        getContext().become(waitForSectorAck);
        process(message);
    }

    private void process(PlayerCommand.AssignSection message) {
        if (sectionState.hasSection()) {
            log.debug("Unregister player from previous sector [{}]", sectionState.current());
            sectionState.tell(new SectionMessage.UnregisterPlayer(self(), player));

        }
        log.debug("Registered at new sector [{}]", message.sectionNo);


        var sectionActor = actorLookup.findSection(message.sectionNo);

        sectionState.transitionTo(new SectionInfo(sectionActor, message.sectionNo));


        sectionActor.tell(new SectionMessage.RegisterPlayer(playerInfo), self());
    }

    private void processWaitSessionAck(SessionMessage.RegisterPlayer.Ack ack) {
        log.debug("Registration with session [{}] acknowledged", ack.sessionActor);
        sessionState.assignSession(ack.sessionActor);
        sessionState.setRegistrationDone();

        sessionState.tell(new PlayerMessage.UnitStatus(unitBank.balance()));

        if (sectionState.hasSection()) {
            log.debug("Session [{}] acknowledged registration, and we have a sector, switching state to playing and informing session about the sector", ack.sessionActor);

            sessionState.tell(new SessionMessage.PlayerAtSection(playerInfo, sectionState.current()));

            getContext().become(playing);
        } else {
            log.debug("Session {{}] acknowledged registration, player lacks sector switching to state WaitForSector", ack.sessionActor);
            if (sectionState.isInTransition()) {
                getContext().become(waitForSectorAck);
            } else {
                getContext().become(waitForSector);
            }
        }
    }

    private void processWaitSectorAck(SessionMessage.RegisterPlayer.Ack ack) {
        log.debug("Registration with session [{}] acknowledged", ack.sessionActor);
        sessionState.assignSession(ack.sessionActor);
        sessionState.setRegistrationDone();

        if (sectionState.hasSection()) {
            log.debug("Session [{}] acknowledged registration, and we have a sector, switching state to playing and informing session about the sector", ack.sessionActor);

            sessionState.tell(new SessionMessage.PlayerAtSection(playerInfo, sectionState.current()));

            getContext().become(playing);
        }
        log.debug("Session {{}] acknowledged registration, player lacks sector still waiting to Section Ack", ack.sessionActor);
    }


    private void processWaitSectorAck(SectionMessage.RegisterPlayer.Ack message) {

        log.debug("Player is now registered at Section [{}] ", message.sector);
        sectionState.assignSection(message.sector);

        if (sessionState.hasSession()) {
            //Inform the session we have moved
            sessionState.tell(new SessionMessage.PlayerAtSection(this.playerInfo, sectionState.current()));
            //And start playing
            getContext().become(playing);
        } else {
            if (sessionState.isWaitingForRegistrationAck()) {
                log.debug("Player is still waiting for Ack that he his registered, can't inform session about newly assigned sector will switch to waitForSessionAck");
                getContext().become(waitForSessionAck);
            } else {
                log.warning("Player is still waiting to be assigned to session by the GameActor?, can't inform session about newly assigned sector will switch to waitForSession");
                getContext().become(waitForSession);
            }
        }
    }

    private void processAllStates(SessionTerminated sessionTerminated) {
        boolean affected = sessionState.sessionTerminate(sessionTerminated);
        if (affected) {
            log.info("Current session terminated");
        }
    }

    private void processAllStates(SectionMessage.UnregisterPlayer.Ack unregisterOk) {
        log.debug("Section [{}] confirmed un-registration", unregisterOk.sector);
    }

    @Override
    public Receive createReceive() {
        return this.waitForSession;
    }

    private class SessionState {

        private ActorRef sessionActor;
        private boolean waitingForRegistration = false;

        private SessionState() {
            sessionActor = null;
        }

        private void assignSession(ActorRef sessionActor) {
            assert this.sessionActor == null : "Can't assign new session before unassign has been done";
            assert sessionActor != null;
            this.sessionActor = sessionActor;
            this.waitingForRegistration = false;
            ActorPath teamsPath = context().parent().path().child(TeamsActor.ActorName);
            var teamsRef = context().actorSelection(teamsPath).resolveOneCS(gameDefinition.standardTimeout());
            teamsRef.thenAccept(teamsActor -> {
                teamsActor.tell(new TeamsMessage.ActivatePlayer(playerInfo), self());
            });

            log.info("Player attached to session [{}], now  active");


            getContext().watchWith(sessionActor, new SessionTerminated(sessionActor));
        }

        private boolean isWaitingForRegistrationAck() {
            return waitingForRegistration;
        }

        private void setWaitingForRegistration() {
            waitingForRegistration = true;
        }

        private void setRegistrationDone() {
            this.waitingForRegistration = false;
        }

        private void unassignSession() {
            waitingForRegistration = false;
            if (this.sessionActor != null) {
                getContext().unwatch(sessionActor);
            }


            ActorPath teamsPath = context().parent().path().child(TeamsActor.ActorName);
            var teamsRef = context().actorSelection(teamsPath).resolveOneCS(gameDefinition.standardTimeout());
            teamsRef.thenAccept(teamsActor -> {
                teamsActor.tell(new TeamsMessage.DeactivatePlayer(playerInfo), self());
            });
            var tmp = sessionActor;
            this.sessionActor = null;
            log.info("Player detached from session [{}], now inactive", tmp);
        }

        private boolean hasSession() {
            return sessionActor != null;
        }

        private boolean sessionTerminate(SessionTerminated sessionTerminated) {
            if (sessionTerminated.session.equals(sessionActor)) {
                log.info("Current session terminated");
                unassignSession();
                getContext().become(waitForSession, true);
                return true;
            } else {
                return false;
            }
        }

        void tell(Object msg) {
            sessionActor.tell(msg, self());
        }

    }

    static class SessionTerminated {
        private final ActorRef session;

        SessionTerminated(ActorRef session) {
            this.session = session;
        }
    }


    class SectionState {
        private ActorRef sectionActor;
        private int sectionNo = -1;
        private SectionInfo transitionTo = null;

        SectionInfo assignSection(ActorRef sector, int sectorNo) {
            var prev = current();
            this.sectionActor = sector;
            this.sectionNo = sectorNo;
            transitionTo = null;
            return prev;
        }

        SectionInfo assignSection(SectionInfo newSector) {
            return assignSection(newSector.sectionActor, newSector.sectionNo);
        }

        SectionInfo current() {
            return new SectionInfo(sectionActor, sectionNo == -1 ? null : sectionNo);
        }

        void transitionTo(SectionInfo sectionInfo) {
            this.transitionTo = sectionInfo;
        }

        void unassignSection() {
            sectionActor = null;
            sectionNo = -1;
        }

        boolean hasSection() {
            return sectionNo != -1 && transitionTo == null;
        }

        void tell(Object msg) {
            sectionActor.tell(msg, self());
        }

        private boolean isInTransition() {
            return transitionTo != null;
        }
    }


}
