package messages;

import akka.actor.ActorRef;
import com.google.common.base.MoreObjects;

public abstract class SessionMessage {

    public static class OnNewConnection extends SessionMessage {
        public final ActorRef connection;

        public OnNewConnection(ActorRef connection) {

            this.connection = connection;
        }
    }


    /**
     * Player sends this to session, sends back an Ack to player
     */
    public static class RegisterPlayer extends SessionMessage {
        public final PlayerInfo playerInfo;


        public RegisterPlayer(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("playerInfo", playerInfo)
                    .toString();
        }

        public static class Ack extends SessionMessage {
            public final ActorRef sessionActor;

            public Ack(ActorRef sessionActor) {
                this.sessionActor = sessionActor;
            }
        }
    }

    /**
     * Inform session that the player is at a sectionNo
     */
    public static class PlayerAtSection extends SessionMessage {
        public final PlayerInfo playerInfo;
        public final SectionInfo sectionInfo;


        public PlayerAtSection(PlayerInfo playerInfo, SectionInfo currentSectionInfo) {
            this.playerInfo = playerInfo;
            this.sectionInfo = currentSectionInfo;

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("playerInfo", playerInfo)
                    .add("sectionInfo", sectionInfo)
                    .toString();
        }
    }

    /**
     * An error has occurred, kill the session
     */
    public static class SessionTerminate extends SessionMessage {
        public final Throwable error;

        public SessionTerminate(Throwable error) {
            this.error = error;
        }
    }

    /**
     * The command sent by the client was not ok
     */
    public static class BadClientCommand extends SessionMessage {
        public final String errorMessage;

        public BadClientCommand(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("errorMessage", errorMessage)
                    .toString();
        }
    }

    /**
     * Ask the session to ask the Client for a player name
     */
    public static class RequestClientPlayerName extends SessionMessage {
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    }

}
