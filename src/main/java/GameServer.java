import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.WebSocketDirectives;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import games.IJson;
import messages.SessionMessage;
import messages.client.AkkaWireMessage;

import java.util.concurrent.CompletionStage;

class GameServer extends WebSocketDirectives {

    private final ActorSystem actorSystem;
    private final IWebSocketSessionCallbacks sessionFactory;
    private final IJson json;

    private CompletionStage<ServerBinding> binding;

    GameServer(ActorSystem actorSystem, IWebSocketSessionCallbacks sessionFactory, IJson json) {
        this.actorSystem = actorSystem;
        this.sessionFactory = sessionFactory;
        this.json = json;
    }


    void start() {
        final Http http = akka.http.javadsl.Http.get(actorSystem);
        final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routes().flow(actorSystem, materializer);
        binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("127.0.0.1", 8082), materializer);

    }

    CompletionStage<Done> stop() {
        return binding.thenCompose(ServerBinding::unbind); // trigger unbinding from the port clever thing

        //. .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }


    private Route routes() {

        return route(
                path("ws", () -> handleWebSocketMessages(createWebSocketFlow())),

                path("hello", () ->
                        get(() ->
                                complete(StatusCodes.OK, HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<html><h1>Say hello to akka-http</h1></html>")))),

                pathPrefix("web", () -> getFromResourceDirectory("web")),
                pathPrefix("dev", () -> getFromDirectory("D:\\Dev\\overlord\\src\\main\\resources\\web"))
        );

    }


    /**
     * NOTE THIS METHOD GETS CALLED EVERY TIME WE HAVE A NEW CONNECTION SO ITS A GOOD PLACE TO CREATE THE SESSION ACTOR
     * AND HOOK IT UP WITH THE WEB SOCKET OVER SOME "Magic actor"
     *
     * @return a magic flow gluing the whole thing with actor in between
     */
    private Flow<Message, Message, NotUsed> createWebSocketFlow() {

            /*


            Source<Message, SourceQueueWithComplete<Message>> source2 = Source.<Message>queue(5, OverflowStrategy.fail());
            Source<Message,ActorRef> source3 = source2.mapMaterializedValue(sourceQueue ->
                    getContext().actorOf(SessionActor.props(uuid, gameDefinition, self(), new MyQueueWrapper(sourceQueue)))
            );*/

        //final ActorRef sessionActor = getContext().actorOf(SessionActor.props(uuid, gameDefinition, self()));
        //activeSessions.add(sessionActor);

        final ActorRef sessionActor = this.sessionFactory.createSessionActor();

        //This createInSection an actor representing the client "connections" by sending messages to the destinationRef we can
        Source<Message, NotUsed> source = Source.<Message>actorRef(5, OverflowStrategy.fail())
                .mapMaterializedValue(destinationRef -> {
                    sessionActor.tell(new SessionMessage.OnNewConnection(destinationRef), ActorRef.noSender());//tell the session about the connection
                    return NotUsed.getInstance();
                });


        //The Sink controls what to do with the incoming messages we receive from the client
        Sink<Message, NotUsed> sink = Flow.<Message>create()
                .map(AkkaWireMessage::new)
                .map(awm -> awm.parse(json))
                .to(Sink.actorRef(sessionActor, PoisonPill.getInstance()));
        return Flow.fromSinkAndSource(sink, source);
    }


    interface IWebSocketSessionCallbacks {

        ActorRef createSessionActor();

    }
}
