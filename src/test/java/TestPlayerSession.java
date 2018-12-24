import akka.actor.ActorRef;
import akka.testkit.TestKit;
import games.IPlayerSink;
import messages.ClientCommand;
import messages.GameMessage;
import org.junit.jupiter.api.Test;


public class TestPlayerSession extends Common {

    @Test
    public void testPlayerAdd() throws Exception {

        //Create the game
        final TestKit testProbe = new TestKit(system);
        final ActorRef gameActor = system.actorOf(GameActor.props(gameDefinition), "TheGame");
        final IPlayerSink sink = msg1 -> System.out.println(String.format("Sink received %s", msg1));

        //Assume session register it self to game
        GameMessage.NewSession msg = new GameMessage.NewSession();
        gameActor.tell(msg, testProbe.testActor());
        var sessionOk = testProbe.expectMsgClass(GameMessage.NewSession.Ok.class);

        //Then login a player
        GameMessage.LoginPlayer loginPlayer = new GameMessage.LoginPlayer(sessionOk.sessionActor, "PlyrOne");

        gameActor.tell(loginPlayer, testProbe.testActor());

        //the game inform playerActor about its session
        //the game assigns a sectionNo to the playerActor
        //the game replies to session that the session now has a Player

        var playerLoginOk = testProbe.expectMsgClass(GameMessage.LoginPlayer.Ok.class);

        //Now move the Cursor
        var sessionActor = sessionOk.sessionActor;
        sessionActor.tell(new ClientCommand.MoveCursor(gameDefinition.worldDefinition().sectionDefinition.randomCord()), testProbe.testActor());
        Thread.sleep(300);
        sessionActor.tell(new ClientCommand.MoveCursor(gameDefinition.worldDefinition().sectionDefinition.randomCord()), testProbe.testActor());
        sessionActor.tell(new ClientCommand.MoveCursor(gameDefinition.worldDefinition().sectionDefinition.randomCord()), testProbe.testActor());


        Thread.sleep(8000);
    }
}
