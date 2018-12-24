import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import games.GameDefinition;
import games.GameDefinitions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Common {

    static GameDefinition gameDefinition = GameDefinitions.SMALL;
    static LoggingAdapter log;


    /**
     * public static AkkaJUnitActorSystemResource actorSystemResource =
     * new AkkaJUnitActorSystemResource("DistributedPubSubMediatorTest",
     * ConfigFactory.parseString(
     * "akka.actor.provider = \"cluster\"\n" +
     * "akka.remote.netty.tcp.port=0\n" +
     * "akka.remote.artery.canonical.port=0"));
     */
    protected ActorSystem system;

    @BeforeEach
    void setUp() {
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

        Config cfg = ConfigFactory.parseProperties(properties);

        system = ActorSystem.create("TestGameActor", cfg);

        log = Logging.getLogger(system, this);
    }

    @AfterEach
    void tearDown() {
        TestKit.shutdownActorSystem(system, Duration.apply(5, TimeUnit.SECONDS), true);
        system = null;
    }
}
