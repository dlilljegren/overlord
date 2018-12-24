import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Main {

    public static void main(String[] args) throws java.io.IOException {
        ActorSystem system = ActorSystem.create("testSystem");

        try {
            // Create top level supervisor
            ActorRef supervisor = system.actorOf(MyActor.props(), "my-actor");

            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }


    static class MyActor extends AbstractActor {
        private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        public static Props props() {
            return Props.create(MyActor.class);
        }

        public Receive createReceive() {
            return receiveBuilder()
                    .build();
        }

        @Override
        public void preStart() {
            log.info("MyActor Application started");
        }

        @Override
        public void postStop() {
            log.info("MyAxtor Application stopped");
        }
    }
}

