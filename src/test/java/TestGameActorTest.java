import akka.actor.*;
import akka.pattern.Patterns;
import akka.testkit.TestKit;
import akka.util.Timeout;
import games.GameDefinition;
import games.GameDefinitions;
import messages.GameMessage;
import messages.SectionMessage;
import org.junit.jupiter.api.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import world.Cord;
import world.Teams;
import world.Unit;

import java.util.concurrent.TimeUnit;

class TestGameActorTest extends Common {

    static Unit Unit = new Unit(Teams.teamForName("red"));

    static GameDefinition gameDefinition = GameDefinitions.SMALL;


    @Test
    void testGameActor() throws InterruptedException {
        final TestKit testProbe = new TestKit(system);
        final ActorRef gameActor = system.actorOf(GameActor.props(gameDefinition), "TheGame");


        //GameMessage.NewSession msg = new GameMessage.NewSession(msg1 -> System.out.println(String.format("Sink received %s", msg1)));
        GameMessage.NewSession msg = new GameMessage.NewSession();//ToDo
        gameActor.tell(msg, testProbe.testActor());
        var sessionOk = testProbe.expectMsgClass(GameMessage.NewSession.Ok.class);

        //Login player to the session
        var sessionActor = sessionOk.sessionActor;
        GameMessage.LoginPlayer loginPlayer = new GameMessage.LoginPlayer(sessionActor, "PlayerOne");
        gameActor.tell(loginPlayer, testProbe.testActor());


        testProbe.expectMsgClass(GameMessage.LoginPlayer.Ok.class);

        Thread.sleep(2000);
    }

    @Test
    void testSectionAddUnit() throws Exception {
        final TestKit testProbe = new TestKit(system);
        final ActorRef gameActor = system.actorOf(GameActor.props(gameDefinition));

        Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(2, TimeUnit.SECONDS));
        Future<Object> reply = Patterns.ask(gameActor, new Identify("Check"), timeout);
        ActorIdentity rep = (ActorIdentity) Await.result(reply, timeout.duration());

        assert log != null;
        log.info("Got identity " + rep);

        ActorRef section0 = section(gameActor, 0);

        SectionMessage.TryAddUnit msg = new SectionMessage.TryAddUnit(new Unit(Teams.teamForName("red")), Cord.at(0, 0), testProbe.testActor());
        section0.tell(msg, testProbe.testActor());
        testProbe.expectMsgClass(Status.Failure.class);

        //add again
        section0.tell(msg, testProbe.testActor());
        testProbe.expectMsgClass(Status.Failure.class);

        //add at invalid cord
        SectionMessage.TryAddUnit invalidMsg = new SectionMessage.TryAddUnit(new Unit(Teams.teamForName("red")), Cord.at(30, 30), testProbe.testActor());
        section0.tell(invalidMsg, testProbe.testActor());
        testProbe.expectMsgClass(Status.Failure.class);


        //add to master at cord that has 3 slaves
        section0.tell(new SectionMessage.TryAddUnit(Unit, Cord.at(15, 18), testProbe.testActor()), testProbe.testActor());

        //add to slave, it should forward to master
        ActorRef section4 = section(gameActor, 4);
        SectionMessage.TryAddUnit msgToSlave = new SectionMessage.TryAddUnit(Unit, Cord.at(0, 0), testProbe.testActor());
        section4.tell(msgToSlave, testProbe.testActor());
    }

    private ActorRef section(ActorRef gameActor, int sectionNo) throws Exception {

        ActorPath sectionPath = gameActor.path().child("Section-" + sectionNo);

        Future<ActorRef> ref = system.actorSelection(sectionPath).resolveOne(FiniteDuration.apply(5, TimeUnit.SECONDS));
        return Await.result(ref, FiniteDuration.apply("2seconds"));
    }


}