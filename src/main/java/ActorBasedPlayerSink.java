import akka.actor.ActorRef;
import games.IPlayerSink;
import messages.DataMessage;

public class ActorBasedPlayerSink implements IPlayerSink {

    public ActorBasedPlayerSink(ActorRef websocketActor) {
    }

    @Override
    public void sendMessage(DataMessage msg) {

    }
}
