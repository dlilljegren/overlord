package messages;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;
import world.Cord;
import world.WorldCord;

import javax.annotation.Nullable;

/**
 * Messages send by the Web Client requesting something
 */
public abstract class ClientCommand {

    public static class AddUnit extends ClientCommand {
        public final WorldCord cord;

        public AddUnit(WorldCord cord) {
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .toString();
        }
    }

    public static class RemoveUnit extends ClientCommand {
        public final Cord cord;

        public RemoveUnit(Cord cord) {
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .toString();
        }
    }

    public static class MoveCursor extends ClientCommand {
        public final Cord cord;

        public MoveCursor(Cord cord) {
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .toString();
        }
    }

    public static class GoToSection extends ClientCommand {
        public final int sectionNo;

        public GoToSection(int sectionNo) {
            this.sectionNo = sectionNo;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sectionNo", sectionNo)
                    .toString();
        }
    }

    /**
     * Request session info
     */
    public static class SessionInfo extends ClientCommand {


    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class Login extends ClientCommand {
        public final String userCookie;

        @Nullable
        public final String playerName;

        public Login(String userCookie, String playerName) {
            this.userCookie = userCookie;
            this.playerName = playerName;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("userCookie", userCookie)
                    .toString();
        }
    }
}
