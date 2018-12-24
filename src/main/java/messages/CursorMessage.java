package messages;


import akka.actor.ActorRef;
import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.google.common.base.MoreObjects;
import world.Cord;
import world.Team;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public abstract class CursorMessage {


    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class MoveCursor extends CursorMessage {
        public final Team team;
        public final String playerName;
        public final Cord position;

        @JsonAttribute(ignore = true, nullable = true)
        public final ActorRef session;

        public MoveCursor(Team team, String playerName, Cord position, ActorRef session) {
            this.team = team;
            this.playerName = playerName;
            this.position = position;
            this.session = session;

        }

        @CompiledJson
        public MoveCursor(Team team, String playerName, Cord position) {
            this(team, playerName, position, null);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("team", team)
                    .add("playerName", playerName)
                    .add("position", position)
                    .add("session", session)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MoveCursor that = (MoveCursor) o;
            return Objects.equals(team, that.team) &&
                    Objects.equals(playerName, that.playerName) &&
                    Objects.equals(position, that.position) &&
                    Objects.equals(session, that.session);
        }

        @Override
        public int hashCode() {
            return Objects.hash(team, playerName, position, session);
        }
    }

    public static class RemoveSession extends CursorMessage {
        public final ActorRef session;

        public RemoveSession(ActorRef session) {
            this.session = session;
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class CursorState extends CursorMessage {
        public final Map<UUID, MoveCursor> cursors;
        public final int sectionNo;

        public CursorState(int sectionNo, Map<UUID, MoveCursor> cursors) {
            this.sectionNo = sectionNo;
            this.cursors = cursors;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CursorState that = (CursorState) o;
            return sectionNo == that.sectionNo &&
                    Objects.equals(cursors, that.cursors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cursors, sectionNo);
        }
    }


}
