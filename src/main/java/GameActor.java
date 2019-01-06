import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.PatternsCS;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import games.GameDefinition;
import games.GameDefinitions;
import messages.*;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import util.Namer;
import world.SectorNeighbours;
import world.Team;
import world.Teams;
import world.WorldDefinition;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GameActor extends AbstractActor implements GameServer.IWebSocketSessionCallbacks {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ActorRef> playerNameToActor = new HashMap();

    private final Map<String, String> cookieToPlayer = new HashMap<>();

    private final List<ActorRef> activeSessions = new ArrayList<>();

    private Map<Integer, ActorRef> sectionNoToActor;

    private final GameDefinition gameDefinition;
    private final WorldDefinition worldDefinition;

    private ActorRef teamsActor;

    public static Props props(GameDefinition gameDefinition) {
        return Props.create(GameActor.class, gameDefinition);
    }

    private final GameServer gameServer;

    private final Namer sessionNamer = new Namer("session");

    private GameActor(GameDefinition gameDefinition) {
        this.gameDefinition = gameDefinition;
        this.worldDefinition = gameDefinition.worldDefinition();
        this.gameServer = new GameServer(getContext().getSystem(), this, gameDefinition.serializer());
    }

    private ActorRef section(int sectionNo) {
        return sectionNoToActor.get(sectionNo);
    }

    @Override
    public void preStart() {

        log.info("GameActor preStart, creating SectionActors");
        sectionNoToActor = this.gameDefinition.createSections().collect(Collectors.toMap(
                section -> section.sectionNo,
                section -> getContext().actorOf(SectionActor.props(section, gameDefinition), ActorNames.sectionName(section.sectionNo))));

        sectionNoToActor.forEach((n, sectionActor) -> {
            log.info("Sending neighbours to sectionNo [{}]", sectionActor.path());
            SectorNeighbours sn = SectorNeighbours.create(worldDefinition, n);
            messages.SectionNeighbours msg = new messages.SectionNeighbours(
                    sn.masters().stream().collect(Collectors.toMap(i -> i, i -> sectionNoToActor.get(i))),
                    sn.slaves().stream().collect(Collectors.toMap(i -> i, i -> sectionNoToActor.get(i))),
                    sn.normals().stream().collect(Collectors.toMap(i -> i, i -> sectionNoToActor.get(i)))
            );
            sectionActor.tell(msg, self());
        });

        teamsActor = getContext().actorOf(TeamsActor.props(gameDefinition), TeamsActor.ActorName);

        populateBase(IntSets.immutable.of(0, 1, 5), Teams.RED);
        populateBase(IntSets.immutable.of(15, 20, 21), Teams.BLUE);
        populateBase(IntSets.immutable.of(3, 4, 9), Teams.BLACK);


        log.info("Starting WebServer");
        gameServer.start();


        log.info("Server Started");


    }

    private void populateBase(IntSet sections, Team team) {
        var msg = new SectionMessage.PopulateRandomBase(team);
        sections.collect(i -> sectionNoToActor.get(i))
                .forEach(sa -> sa.tell(msg, self()));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GameMessage.NewSession.class, this::process)
                .match(GameMessage.LoginPlayer.class, this::process)
                .match(PlayerMessage.SessionAck.class, this::process)
                .match(ClientCommand.Login.class, this::process)//Client wants a player based on previous cookie
                .match(Terminated.class, this::process)
                .matchAny(o -> log.info("received unknown message [{}]", o))
                .build();
    }


    private void process(PlayerMessage.SessionAck sessionAck) {
        log.info("Player [{}] ack session assigned [{}]", sender(), sessionAck);
    }

    private void process(GameMessage.NewSession newSession) {
        //Create the session
        UUID sessionId = UUID.randomUUID();
        ActorRef sessionActor = getContext().actorOf(SessionActor.props(sessionId, gameDefinition, self()), ActorNames.sessionName(sessionId));
        //sessionActor.tell(new SessionMessage.OnNewConnection()

        activeSessions.add(sessionActor);

        sender().tell(new GameMessage.NewSession.Ok(sessionActor), self());
        log.info("Created session [{}]", sessionActor);
    }


    /**
     * Login player to a session
     *
     * @param loginPlayer
     */
    private void process(GameMessage.LoginPlayer loginPlayer) {
        ActorRef playerActor = getOrCreatePlayerActor(loginPlayer.playerName);


        //Inform the playerActor about the session
        log.info("Assigning session to Player");
        playerActor.tell(new GameMessage.AssignSession(loginPlayer.session), self());

        //Tell the playerActor to go to sectionNo
        int sectionNo = this.worldDefinition.randomSectionNo();

        playerActor.tell(new PlayerCommand.AssignSection(sectionNo), self());//Tell playerActor its now in sectionNo

        sender().tell(new GameMessage.LoginPlayer.Ok(playerActor), self());

    }

    /**
     * The watch done in createSessionActor results in a Terminate message
     *
     * @param terminated the
     */
    private void process(Terminated terminated) {
        var sessionActor = terminated.actor();
        var wasRemoved = activeSessions.remove(sessionActor);
        if (!wasRemoved) {
            log.warning("Got terminate on actor [{}] no matching session actor found in activeSessions", sessionActor.path());
        } else {
            log.info("Removed sessionActor [{}]", sessionActor);
        }
    }

    @Override
    public ActorRef createSessionActor() {
        UUID uuid = UUID.randomUUID();
        final ActorRef sessionActor = getContext().actorOf(SessionActor.props(uuid, gameDefinition, self()), ActorNames.sessionName(uuid));

        activeSessions.add(sessionActor);

        getContext().watch(sessionActor);

        return sessionActor;

    }


    /**
     * Session has forwarded a message from the client with the client's cookie, this methods job is to see if we already have the cookie mapped to a player
     *
     * @param cookieLogin
     */
    private void process(ClientCommand.Login cookieLogin) {
        var cookie = cookieLogin.userCookie;

        if (cookie == null) {
            sender().tell(new SessionMessage.BadClientCommand("ClientCommand.Login.userCookie missing"), self());
            return;
        }

        var playerName = this.cookieToPlayer.get(cookie);
        if (playerName == null) {
            playerName = cookieLogin.playerName;
            cookieToPlayer.put(cookie, playerName);
        }
        if (playerName == null) {
            sender().tell(new SessionMessage.RequestClientPlayerName(), self());
            return;
        }

        var playerActor = getOrCreatePlayerActor(playerName);

        log.info("Assigning session to Player");
        playerActor.tell(new GameMessage.AssignSession(sender()), self());

        //Tell the playerActor to go to sectionNo
        int sectionNo = this.worldDefinition.randomSectionNo();
        playerActor.tell(new PlayerCommand.AssignSection(sectionNo), self());//Tell playerActor its now in sectionNo

        sender().tell(new GameMessage.LoginPlayer.Ok(playerActor), self());

    }

    private ActorRef getOrCreatePlayerActor(String playerName) {
        ActorRef playerActor = playerNameToActor.get(playerName);
        if (playerActor != null) {
            log.info("Player '{}' already exist", playerName);
            return playerActor;
        }

        CompletableFuture<Object> future = PatternsCS.ask(teamsActor, new TeamsMessage.RequestRecommended(), 2000).toCompletableFuture();
        try {
            var reply = (TeamsMessage.RecommendedReply) future.get();
            var team = reply.recommended;
            var player = team.createPlayer(playerName);
            playerActor = getContext().actorOf(PlayerActor.props(gameDefinition, player), ActorNames.playerName(playerName));
            playerNameToActor.put(playerName, playerActor);
            return playerActor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws java.io.IOException {
        Properties properties = new Properties();
        properties.setProperty("akka.actor.provider", "cluster");
        //properties.setProperty("akka.log-config-on-start","on");
        properties.setProperty("akka.loglevel", "DEBUG");
        //properties.setProperty("akka.actor.provider","akka.cluster.ClusterActorRefProvider");
        properties.setProperty("akka.cluster.min-nr-of-members", "1");
        properties.put("akka.extensions", List.of("akka.cluster.pubsub.DistributedPubSub"));
        properties.put("akka.remote.enabled-transports", List.of("akka.remote.netty.tcp"));
        properties.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
        properties.setProperty("akka.remote.netty.tcp.port", "2552");
        properties.put("akka.cluster.seed-nodes", List.of("akka.tcp://TestGameActor@127.0.0.1:2552"));
        properties.put("akka.cluster.pub-sub.send-to-dead-letters-when-no-subscribers", "false");

        //See https://doc.akka.io/docs/akka-http/current/server-side/websocket-support.html
        properties.put("akka.http.server.websocket.periodic-keep-alive-max-idle", "1s");

        Config cfg = ConfigFactory.parseProperties(properties);
        ActorSystem system = ActorSystem.create("GameSystem", cfg);

        try {
            // Create top level supervisor
            //ActorRef supervisor = system.actorOf(MyActor.props(), "my-actor");
            system.actorOf(GameActor.props(GameDefinitions.SMALL), "GameActor");

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
