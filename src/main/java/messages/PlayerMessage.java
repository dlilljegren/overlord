package messages;

import com.google.common.base.MoreObjects;
import world.Player;

import java.util.Map;

public abstract class PlayerMessage {


    public static class UnitStatus extends PlayerMessage {
        public final int units;

        public UnitStatus(int units) {
            this.units = units;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("units", units)
                    .toString();
        }
    }


    public static class SectionAllocatedUnits extends PlayerMessage {
        public final int sectionNo;
        public final Map<AllocationReason, Integer> units;

        public SectionAllocatedUnits(int sectionNo, Map<AllocationReason, Integer> units) {
            assert !units.isEmpty();
            this.sectionNo = sectionNo;
            this.units = units;
        }

        public int totalUnits() {
            return units.values().stream().mapToInt(i -> i).sum();
        }
    }


    public enum AllocationReason {
        Sector,
        Base
    }


    public static class SessionAck extends PlayerMessage {
        private final Player player;

        public SessionAck(Player player) {
            this.player = player;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("player", player)
                    .toString();
        }
    }


}
