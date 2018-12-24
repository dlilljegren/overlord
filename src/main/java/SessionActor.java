import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.ws.TextMessage;
import changes.ViewUpdate;
import com.google.common.collect.Sets;
import games.GameDefinition;
import games.IJson;
import games.IPlayerSink;
import messages.*;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import world.*;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class SessionActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final GameDefinition gameDefinition;
    private final WorldDefinition worldDefinition;
    private final IJson json;

    private IPlayerSink playerSink;
    private final ActorRef gameActor;

    private final Receive waitOnConnection;
    private final Receive waitOnClientLogin;
    private final Receive waitPlayerLogin;
    private final Receive waitPlayerAtSection;
    private final Receive playing;
    private final UUID sessionId;


    private final PlayerState playerState;

    private SectionInfo activeSector;
    private ActorRef currentCursor;


    private final ActorRef mediator;

    private final Set<Integer> subscribedSectors;


    private ViewState viewState;

    private int viewCounter = 0;

    public static Props props(UUID sessionId, GameDefinition gameDefinition, ActorRef gameActor) {
        return Props.create(SessionActor.class, sessionId, gameDefinition, gameActor);
    }

    /**
     * State of actor
     * Init - we have connection to client but no playerActor assigned
     * Playing - we have a playerActor and should receive commands and send updates
     *
     * @param gameDefinition
     */
    private SessionActor(UUID sessionId, GameDefinition gameDefinition, ActorRef gameActor) {
        this.sessionId = sessionId;
        this.gameDefinition = gameDefinition;
        this.worldDefinition = gameDefinition.worldDefinition();
        this.gameActor = gameActor;

        this.playerState = new PlayerState();
        this.activeSector = null;

        this.waitOnConnection = createWaitOnConnection();
        this.waitOnClientLogin = createWaitOnClientLogin();
        this.waitPlayerLogin = createWaitPlayerRegister();
        this.waitPlayerAtSection = createWaitPlayerAtSection();

        this.playing = createPlaying();
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();//AkkaWireMessage bus equivalent for subscribing
        this.subscribedSectors = Sets.newHashSet();


        this.json = gameDefinition.serializer();

    }


    @Override
    public Receive createReceive() {
        return waitOnConnection;
    } // Initial state


    private Receive createWaitOnConnection() {
        return receiveBuilder()
                .match(SessionMessage.OnNewConnection.class, this::process)
                .matchAny(o -> log.info("received unsupported message when in waiting for connection state [{}] from:[{}]", o, sender()))
                .build();
    }

    private Receive createWaitOnClientLogin() {
        return receiveBuilder()
                .match(ClientCommand.Login.class, this::processWaitOnClientLogin)
                .match(Status.Failure.class, f -> log.error(f.cause(), "Failure [{}] ", f))
                .matchAny(o -> log.info("received unsupported message when in waiting for client login state [{}] from:[{}]", o, sender()))
                .build();


    }


    private Receive createWaitPlayerRegister() {
        return receiveBuilder()
                .match(SessionMessage.RegisterPlayer.class, this::processRegisterPlayer)
                .match(SessionMessage.RequestClientPlayerName.class, this::processRequestPlayerName)
                .match(ViewMessage.class, this::process)
                .matchAny(o -> log.info("received unknown message when in waiting for player state [{}] from:[{}]", o, sender()))
                .build();
    }


    private Receive createWaitPlayerAtSection() {
        return receiveBuilder()
                .match(SessionMessage.PlayerAtSection.class, this::processPlayerAtSection)
                .match(PlayerMessage.UnitStatus.class, this::toSink)
                .match(ViewMessage.class, this::process)
                .matchAny(o -> log.info("received unknown message when in WaitPlayerAtSection state [{}] from:[{}]", o, sender()))
                .build();
    }


    private Receive createPlaying() {
        return receiveBuilder()
                .match(CursorMessage.CursorState.class, this::process)//Other players are moving the cursor in this players sector
                .match(ClientCommand.MoveCursor.class, this::process)
                .match(ClientCommand.AddUnit.class, this::process)
                .match(ClientCommand.RemoveUnit.class, this::process)
                .match(ClientCommand.GoToSection.class, this::process)
                .match(SectionReply.Snapshot.class, this::process)
                .match(ClientMessage.AddUnit.class, this::toSink)
                .match(ClientMessage.InfoMsg.class, this::toSink)
                .match(PlayerMessage.UnitStatus.class, this::toSink)
                .match(ViewUpdate.Snapshot.class, this::toSink)
                .match(ViewUpdate.Delta.class, this::toSink)
                .match(SectorCombatStats.class, this::toSink)
                .match(SectionBroadcastMessage.ZoneOfControl.class, this::process)
                .match(SectionBroadcastMessage.CombatInfo.class, this::process)
                .match(DistributedPubSubMediator.SubscribeAck.class, this::process)
                .match(DistributedPubSubMediator.UnsubscribeAck.class, this::process)
                .match(ClientCommand.Login.class, this::process)
                .match(ViewMessage.class, this::process)
                .match(Status.Failure.class, this::process)
                .match(SessionMessage.SessionTerminate.class, this::process)
                .match(PlayerCommand.AddUnit.Failed.class, this::process)
                .matchAny(o -> log.info("received unsupported message [{}] when in state playing", o))
                .build();

    }


    private void process(SessionMessage.SessionTerminate sessionTerminate) {
        log.error(sessionTerminate.error, "Will kill session Error caused by actor [{}]", sender());
        toSink(new ClientMessage.InfoMsg("Internal error"));
        self().tell(akka.actor.Kill.getInstance(), ActorRef.noSender());

    }


    private void resendSelf(SessionMessage.PlayerAtSection p) {
        log.info("Resending [{}] as session was still in login state", p);
        self().forward(p, getContext());
    }

    private void process(SessionMessage.OnNewConnection onNewConnection) {
        log.info("OnNewConnection");
        this.playerSink = new ActorSink(onNewConnection);

        toSink(new ClientMessage.AssignSession(this.sessionId));
        toSink(new ClientMessage.InitGame(gameDefinition.worldDefinition()));
        getContext().become(waitOnClientLogin);
    }

    private void processWaitOnClientLogin(ClientCommand.Login loginMsg) {
        log.info("Client login [{}]", loginMsg);

        toSink(new ClientMessage.InfoMsg("Processing login"));


        //Tell game session about the login so it can map the cookie to a player
        getContext().getParent().tell(loginMsg, self());
        getContext().become(waitPlayerLogin);
    }

    /**
     * Game actor complains he does not have any cookie mapped to a player, the client will need to createInSection a new player by re-doing the login
     *
     * @param requestClientPlayerName
     */
    private void processRequestPlayerName(SessionMessage.RequestClientPlayerName requestClientPlayerName) {
        log.info("GameActor requires a player name");
        toSink(requestClientPlayerName);

        //For now fake a new login from client
        self().tell(new ClientCommand.Login("ABC", "PlayerOne"), self());

        getContext().become(waitOnClientLogin);
    }

    /**
     * Sent by Player to attach to session
     *
     * @param registerPlayer
     */
    private void processRegisterPlayer(SessionMessage.RegisterPlayer registerPlayer) {
        if (registerPlayer.playerInfo.player.equals(playerState.current.player)) {
            log.warning("Player already registered, duplicate message?");

        } else if (playerState.hasPlayer()) {
            throw new RuntimeException("Replacing player not implemented");
        }

        log.info("Register player [{}]", registerPlayer.playerInfo.player);
        playerState.register(registerPlayer.playerInfo.playerActor, registerPlayer.playerInfo.player);

        currentCursor = null;//set to null


        getContext().become(waitPlayerAtSection);
        toSink(new ClientMessage.InfoMsg(format("Player %s registered", registerPlayer.playerInfo.player.name)));

        //Confirm registration
        playerState.tell(new SessionMessage.RegisterPlayer.Ack(self()));

        toSink(new ClientMessage.AssignPlayer(playerState.current.player));
    }


    private void processPlayerAtSection(SessionMessage.PlayerAtSection message) {
        log.info("Player [{}] moved to sectionNo [{}]", message.playerInfo, message.sectionInfo);
        //Unsubscribe to message from previous sector

        var sectionActor = message.sectionInfo.sectionActor;

        if (this.activeSector != null) {

            this.subscribedSectors.remove(this.activeSector.sectionNo);


        }

        this.activeSector = message.sectionInfo;

        //Subscribe to new sector broadcasts
        this.subscribedSectors.add(this.activeSector.sectionNo);
        getContext().become(playing);


        if (this.viewState != null) {
            log.info("Sending stop on existing view actor");
            //Here we should stop subscribing to
            viewState.stop();
        }
        var sectionOrigo = gameDefinition.worldDefinition().section.worldOrigo(activeSector.sectionNo);
        ViewDefinition vd = new ViewDefinition(sectionOrigo, activeSector.sectionNo, 40, 21);
        var viewActor = getContext().actorOf(ViewActor.props(vd, gameDefinition), ActorNames.viewName(vd, viewCounter++));
        viewState = new ViewState(vd, viewActor);


        //Cursor manager
        ActorPath cursorsPath = sectionActor.path().child(SectionCursorsActor.ActorName);
        currentCursor = null;//set to null
        Future<ActorRef> cursorsScalaFuture = context().actorSelection(cursorsPath).resolveOne(FiniteDuration.apply(2, TimeUnit.SECONDS));
        FutureConverters.toJava(cursorsScalaFuture).thenAccept(ar -> currentCursor = ar);


    }


    private void process(SectionBroadcastMessage.CombatInfo combatInfo) {
        if (!(combatInfo instanceof SectionBroadcastMessage.CombatResult))
            this.toSink(combatInfo);
    }

    private void process(SectionBroadcastMessage.ZoneOfControl message) {
        log.debug("Received ZoneOfControl [{}] forwarding to sink", message);
        this.toSink(message);
    }

    private void process(DistributedPubSubMediator.UnsubscribeAck unsubscribeAck) {
        log.info("Unsubscribed ok to topic [{}]", unsubscribeAck.unsubscribe().topic());
    }


    private void process(DistributedPubSubMediator.SubscribeAck subscribeAck) {
        log.info("Subscribed ok to topic [{}]", subscribeAck.subscribe().topic());
    }


    private void requestSnapshot(ActorRef sectionRef) {
        log.info("Requesting snapshot from :[{}]", sectionRef);
        sectionRef.tell(new SectionMessage.RequestSnapshot(), self());
    }


    private void process(CursorMessage.CursorState moveCursor) {
        toSink(moveCursor);
    }

    private void process(ClientCommand.MoveCursor moveCursor) {
        if (currentCursor != null) {
            assert playerState.hasPlayer();

            var team = playerState.current.player.team;
            var playerName = playerState.current.player.name;
            currentCursor.tell(new CursorMessage.MoveCursor(team, playerName, moveCursor.cord, self()), self());
        }

    }

    private void process(SectionReply.Snapshot snapshot) {
        toSink(snapshot.snapshot);
    }

    private void process(ClientCommand.RemoveUnit removeUnit) {
        assert playerState.hasPlayer();
        playerState.tell(removeUnit);
    }

    private void process(ClientCommand.AddUnit addUnit) {
        assert playerState.hasPlayer();
        assert viewState != null;
        var playerCmd = viewState.transform(addUnit);

        playerState.tell(playerCmd);
    }

    private void process(ClientCommand.Login login) {
        toSink(new ClientMessage.InfoMsg("Can't login when in playing state"));
    }

    private void process(ClientCommand.GoToSection goToSection) {
        getContext().become(waitPlayerAtSection);
        playerState.tell(new PlayerCommand.AssignSection(goToSection.sectionNo));
    }

    private void process(ViewMessage viewMessage) {
        if (viewState != null && viewState.isLastMessageCurrentView()) {
            toSink(viewMessage);
        } else {
            log.warning("Got ViewMessage from non active view [{}]", sender());
        }
    }

    private void process(Status.Failure failure) {
        toSink(failure);
    }

    private void process(PlayerCommand.AddUnit.Failed failed) {
        toSink(new ClientMessage.InfoMsg(failed.reason));
    }

    private CompletionStage<ActorRef> section(int sectionNo) {
        ActorPath sectionPath = gameActor.path().child("Section-" + sectionNo);
        Future<ActorRef> ref = getContext().actorSelection(sectionPath).resolveOne(FiniteDuration.apply(5, TimeUnit.SECONDS));
        return FutureConverters.toJava(ref);
    }


    private void toSink(Object msg) {
        assert this.playerSink != null : "playerSink is null";
        var msgType = msg.getClass().getSimpleName();
        var data = msg;


        var dataMsg = new DataMessage(msgType, data);

        this.playerSink.sendMessage(dataMsg);
    }

    @Override
    public void preStart() {

        log.info("Actor started");
    }

    @Override
    public void postStop() {
        log.info("Actor stopped");
    }

    private class PlayerState {


        private PlayerInfo current;

        PlayerState() {
            this.current = new PlayerInfo(null, null);
        }

        PlayerInfo register(ActorRef playerActor, Player player) {
            var prev = current;
            current = new PlayerInfo(playerActor, player);
            return prev;
        }

        PlayerInfo register(PlayerInfo newInfo) {
            return register(newInfo.playerActor, newInfo.player);
        }

        PlayerInfo current() {
            return this.current;
        }

        boolean hasPlayer() {
            return current.playerActor != null;
        }

        void tell(Object msg) {
            assert hasPlayer();
            current.playerActor.tell(msg, self());
        }
    }

    private class ViewState {
        private final View view;
        private final ActorRef viewActor;


        private ViewState(ViewDefinition viewDefinition, ActorRef viewActor) {
            assert viewDefinition != null;
            assert viewActor != null;
            this.viewActor = viewActor;
            this.view = new View(worldDefinition, viewDefinition);

        }


        public void stop() {
            getContext().stop(this.viewActor);
            SessionActor.this.viewState = null;//Set us up for GC
        }

        public boolean isLastMessageCurrentView() {
            return this.viewActor.equals(sender());
        }

        PlayerCommand transform(ClientCommand.AddUnit addUnit) {
            int section = view.masterSection(addUnit.cord);
            var sectionCord = view.viewToSection(section).apply(addUnit.cord);
            return new PlayerCommand.AddUnit(section, sectionCord);
        }


    }

    private class ActorSink implements IPlayerSink {
        private final ActorRef webSocket;


        public ActorSink(SessionMessage.OnNewConnection onNewConnection) {
            webSocket = onNewConnection.connection;
        }


        @Override
        public void sendMessage(DataMessage msg) {
            //var bs = ByteString.fromString(msg);
            //Message wsMsg = new akka.http.scaladsl.model.ws.BinaryMessage.Strict(bs);

            var txt = gameDefinition.serializer().toJson(msg);

            var wsMsg = TextMessage.create(txt);
            webSocket.tell(wsMsg, self());
        }
    }


}
