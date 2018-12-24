import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.TestKit;
import org.junit.jupiter.api.Test;

public class TestPubSub extends Common {


    @Test
    void testPubSub() throws Exception {
        final TestKit testProbe = new TestKit(system);


        system.actorOf(Props.create(Subscriber.class), "subscriber1");
        system.actorOf(Props.create(Subscriber.class), "subscriber2");

        ActorRef publisher = system.actorOf(Props.create(Publisher.class), "publisher");

        publisher.tell("Hej", null);

        Thread.sleep(1000);
    }

    public static class Subscriber extends AbstractActor {
        LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        public Subscriber() {
            ActorRef mediator =
                    DistributedPubSub.get(getContext().system()).mediator();
            // subscribe to the topic named "content"
            mediator.tell(new DistributedPubSubMediator.Subscribe("content", getSelf()),
                    getSelf());
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, msg ->
                            log.info("Got: {}", msg))
                    .match(DistributedPubSubMediator.SubscribeAck.class, msg ->
                            log.info("subscribed"))
                    .build();
        }
    }

    public static class Publisher extends AbstractActor {

        // activate the extension
        ActorRef mediator =
                DistributedPubSub.get(getContext().system()).mediator();

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, in -> {
                        String out = in.toUpperCase();
                        mediator.tell(new DistributedPubSubMediator.Publish("content", out),
                                getSelf());
                    })
                    .build();
        }

    }
}
