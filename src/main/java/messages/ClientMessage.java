package messages;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import world.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public abstract class ClientMessage {


    public static class InitGame extends ClientMessage {

        public SectionDefinition.GridType hexType;
        public int worldWidth;
        public int worldHeight;
        public int sectionWidth;
        public int sectionHeight;

        public InitGame(WorldDefinition worldDefinition) {
            hexType = worldDefinition.sectionDefinition.gridType();
            worldWidth = worldDefinition.gridWidth;
            worldHeight = worldDefinition.gridHeight;
            sectionWidth = worldDefinition.sectionDefinition.cols;
            sectionHeight = worldDefinition.sectionDefinition.rows;
        }

        @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
        public InitGame() {

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("hexType", hexType)
                    .add("worldWidth", worldWidth)
                    .add("worldHeight", worldHeight)
                    .add("sectionWidth", sectionWidth)
                    .add("sectionHeight", sectionHeight)
                    .toString();
        }
    }

    public static class InfoMsg extends ClientMessage {

        public final String message;

        public InfoMsg(String message) {
            requireNonNull(message);
            this.message = message;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("message", message)
                    .toString();
        }
    }

    public static class AssignSession extends ClientMessage {
        @Nonnull
        public final UUID sessionId;

        public AssignSession(UUID sessionId) {
            requireNonNull(sessionId);
            this.sessionId = sessionId;
        }
    }

    public static class AssignPlayer extends ClientMessage {
        @Nonnull
        public final Player player;


        public AssignPlayer(Player player) {
            requireNonNull(player);
            this.player = player;
        }
    }

    private static class UnitMessage extends ClientMessage {

        @Nonnull
        public final WorldCord cord;
        @Nonnull
        public final Unit unit;

        private UnitMessage(@Nonnull WorldCord cord, @Nonnull Unit unit) {
            this.cord = cord;
            this.unit = unit;
        }
    }


    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class AddUnit extends UnitMessage {

        public AddUnit(@Nonnull WorldCord cord, @Nonnull Unit unit) {
            super(cord, unit);
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class RemoveUnit extends UnitMessage {

        public RemoveUnit(@Nonnull WorldCord cord, @Nonnull Unit unit) {
            super(cord, unit);
        }
    }



    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class ZoneOfControl extends ClientMessage {
        @Nonnull
        public final int sectionNo;

        @Nonnull
        public final Rect sectionRect;
        @Nonnull
        public final Map<Team, Set<WorldCord>> gained;

        @Nonnull
        public final Map<Team, Set<WorldCord>> lost;

        @Nonnull
        public final boolean isSnapshot;

        public ZoneOfControl(int sectionNo, Rect sectionRect, Map<Team, Set<WorldCord>> gained, Map<Team, Set<WorldCord>> lost, boolean isSnapshot) {
            this.sectionNo = sectionNo;
            this.sectionRect = sectionRect;
            this.gained = gained;
            this.lost = lost;
            this.isSnapshot = isSnapshot;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sectionNo", sectionNo)
                    .add("isSnapshot", isSnapshot)
                    .add("gained", Maps.transformEntries(gained, (t, cSet) -> cSet.size()))
                    .add("lost", Maps.transformEntries(lost, (t, cSet) -> cSet.size()))
                    .add("sectionRect", sectionRect)
                    .toString();
        }
    }
}
