package messages;

import akka.actor.ActorRef;
import com.google.common.base.MoreObjects;

public abstract class GameMessage {


    public static class NewSession extends GameMessage {

        public NewSession() {
        }

        public static class Ok {
            public final ActorRef sessionActor;

            public Ok(ActorRef sessionActor) {
                this.sessionActor = sessionActor;
            }
        }
    }

    public static class LoginPlayer extends GameMessage {
        public final ActorRef session;
        public final String playerName;

        public LoginPlayer(ActorRef session, String playerName) {
            this.session = session;
            this.playerName = playerName;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("session", session)
                    .add("playerName", playerName)
                    .toString();
        }

        public static class Ok {
            public final ActorRef playerActor;

            public Ok(ActorRef playerActor) {
                this.playerActor = playerActor;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("playerActor", playerActor)
                        .toString();
            }
        }
    }

    public static class AssignSession extends GameMessage {
        public final ActorRef session;

        public AssignSession(ActorRef session) {
            this.session = session;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("session", session)
                    .toString();
        }
    }


}
