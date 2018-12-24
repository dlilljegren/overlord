package messages;

import com.google.common.base.MoreObjects;
import world.Team;

public abstract class TeamsMessage {

    public static class RequestRecommended extends TeamsMessage {

    }

    public static class RecommendedReply {
        public final Team recommended;

        public RecommendedReply(Team recommended) {
            this.recommended = recommended;
        }
    }

    public static class DeactivatePlayer {
        public final PlayerInfo playerInfo;

        public DeactivatePlayer(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("playerInfo", playerInfo)
                    .toString();
        }
    }

    public static class ActivatePlayer {
        public final PlayerInfo playerInfo;

        public ActivatePlayer(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("playerInfo", playerInfo)
                    .toString();
        }
    }
}
