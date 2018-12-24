import akka.NotUsed;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.Optional;

public class TestWebServer {
    //see https://gist.github.com/bentito/560eb95c64fa131efb34ad62c7bf60f8

    private static final class Router extends HttpApp {

        private final ActorSystem system;

        public Router(ActorSystem system) {
            this.system = system;
        }

        public Route routes() {

            return route(
                    path("ws", () -> handleWebSocketMessages(createWebSocketFlow())),

                    path("hello", () ->
                            get(() ->
                                    complete(StatusCodes.OK, HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<html><h1>Say hello to akka-http</h1></html>")))),

                    pathPrefix("web", () -> getFromResourceDirectory("web")),
                    pathPrefix("dev", () -> getFromDirectory("D:\\Dev\\overlord\\src\\main\\resources\\web"))
            );


        }


        private Flow<Message, Message, NotUsed> createWebSocketFlow() {
            ActorRef actor = system.actorOf(Props.create(AnActor.class));//Here one should createInSection a SessionActor waiting for login


            Source<Message, NotUsed> source = Source.<Outgoing>actorRef(5, OverflowStrategy.fail())  //This createInSection an actor representing the client "connection" and informs the session about it
                    .map((outgoing) -> (Message) TextMessage.create(outgoing.message))
                    .<NotUsed>mapMaterializedValue(destinationRef -> {
                        actor.tell(new OutgoingDestination(destinationRef), ActorRef.noSender());
                        return NotUsed.getInstance();
                    });

            Sink<Message, NotUsed> sink = Flow.<Message>create()
                    .map((msg) -> new Incoming(msg.asTextMessage().getStrictText()))
                    .to(Sink.actorRef(actor, PoisonPill.getInstance()));
            return Flow.fromSinkAndSource(sink, source);
        }


        public static void main(String[] args) throws Exception {
            ActorSystem system = ActorSystem.create();

            Router router = new Router(system);

            router.startServer("127.0.0.1", 8082, system);
        }
    }

    static class Incoming {
        public final String message;

        public Incoming(String message) {
            this.message = message;
        }
    }

    static class Outgoing {
        public final String message;

        public Outgoing(String message) {
            this.message = message;
        }
    }

    static class OutgoingDestination {
        public final ActorRef destination;

        OutgoingDestination(ActorRef destination) {
            this.destination = destination;
        }
    }

    static class ServerPush {
        public final String message;

        public ServerPush(String message) {
            this.message = message;
        }
    }

    /**
     * Actor getting created on each new connection
     */
    static class AnActor extends AbstractActorWithTimers {
        private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        private Optional<ActorRef> outgoing = Optional.empty();

        public AnActor() {
            log.info("Creating new Actor");

            getTimers().startPeriodicTimer(
                    "TIMER_KEY",
                    new ServerPush("{\"cmd\":\"testPush\"}"), Duration.ofMillis(3000));


        }

        public Receive createReceive() {
            var reply = "{\"cmd\":\"loginOk\",\"session\":{\"sessionId\":\"AAABBB\"}}";

            return receiveBuilder()
                    .match(OutgoingDestination.class, (msg) -> outgoing = Optional.of(msg.destination)) //Set the pipe to send messages
                    .match(ServerPush.class, msg -> outgoing.ifPresent((out) -> out.tell(new Outgoing(msg.message), self())))
                    .match(Incoming.class, (in) -> outgoing.ifPresent((out) -> out.tell(new Outgoing(reply), self())))
                    .matchAny(o -> log.warning("received unknown message [{}] when in combat state", o))
                    .build();
        }

        @Override
        public void postStop() throws Exception {
            super.postStop();
            log.info("In PostStop");
        }
    }
}
