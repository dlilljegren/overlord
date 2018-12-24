package messages;

import akka.actor.ActorRef;
import com.google.common.base.MoreObjects;
import world.Player;

import java.util.Objects;


public class PlayerInfo {
    public final ActorRef playerActor;
    public final Player player;


    public PlayerInfo(ActorRef playerActor, Player player) {
        this.playerActor = playerActor;
        this.player = player;
    }

    public boolean isEmpty() {
        return this.playerActor == null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("playerActor", playerActor)
                .add("player", player)
                .toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo that = (PlayerInfo) o;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
