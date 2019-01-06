package messages;

import akka.actor.ActorRef;
import com.google.common.base.MoreObjects;
import world.*;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class SectionMessage {

    public static class TryAddUnit extends SectionMessage {


        @Nonnull
        public final Unit unit;
        @Nonnull
        public final Cord cord;
        @Nonnull
        public final ActorRef originator;

        public TryAddUnit(Unit unit, Cord cord, ActorRef originator) {
            this.unit = requireNonNull(unit);
            this.cord = requireNonNull(cord);
            this.originator = requireNonNull(originator);

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("unit", unit)
                    .add("cord", cord)
                    .toString();
        }

        public abstract static class Failed extends SectionMessage {
            private final String errorMessage;
            private final int section;

            protected Failed(int section, String errorMessage) {
                this.section = section;
                this.errorMessage = errorMessage;
            }
        }


    }

    public static class TryRemoveUnit extends SectionMessage {
        public final Team team;
        public final Cord cord;

        public TryRemoveUnit(Team team, Cord cord) {
            this.team = team;
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("team", team)
                    .add("cord", cord)
                    .toString();
        }

    }


    public static class SlaveSetUnit extends SectionMessage {
        public final Unit unit;
        public final Cord cord;

        public SlaveSetUnit(Unit unit, Cord cord) {
            this.unit = unit;
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("unit", unit)
                    .add("cord", cord)
                    .toString();
        }
    }

    public static class SlaveRemoveUnit extends SectionMessage {
        public final Unit unit;
        public final Cord cord;

        public SlaveRemoveUnit(Unit unit, Cord cord) {
            this.unit = unit;
            this.cord = cord;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("unit", unit)
                    .add("cord", cord)
                    .toString();
        }
    }

    public static class RegisterPlayer extends SectionMessage {
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

        public static class Ack extends SectionMessage {
            public final SectionInfo sector;

            public Ack(SectionInfo sector) {
                this.sector = requireNonNull(sector);
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("sector", sector)
                        .toString();
            }
        }

    }

    public static class UnregisterPlayer extends SectionMessage {
        public final ActorRef playerActor;
        public final Player player;

        public UnregisterPlayer(ActorRef playerActor, Player player) {
            this.player = requireNonNull(player);
            this.playerActor = requireNonNull(playerActor);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("playerActor", playerActor)
                    .add("player", player)
                    .toString();
        }

        public static class Ack extends SectionMessage {
            public final SectionInfo sector;

            public Ack(SectionInfo sector) {
                this.sector = requireNonNull(sector);
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("sector", sector)
                        .toString();
            }
        }


    }


    public static class StartCombat extends SectionMessage {
        public final SectorTotalCombatStats totalCombatStats;


        public StartCombat(SectorTotalCombatStats combatStats) {
            this.totalCombatStats = combatStats;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("totalCombatStats", totalCombatStats)
                    .toString();
        }
    }

    public static class RequestSnapshot extends SectionMessage {

    }

    public static class CalculateZoneOfControl extends SectionMessage {
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    }


    public static class StopCombat extends SectionMessage {
        public final SectorCombatStats combatStats;

        public StopCombat(SectorCombatStats combatStats) {
            this.combatStats = combatStats;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("combatStats", combatStats)
                    .toString();
        }
    }

    public static class Ok extends SectionMessage {
        public final int sectionNo;

        public Ok(int sectionNo) {
            this.sectionNo = sectionNo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ok ok = (Ok) o;
            return sectionNo == ok.sectionNo;
        }

        @Override
        public int hashCode() {

            return Objects.hash(sectionNo);
        }
    }

    /**
     * Sent by game server to populate a random base with units
     */
    public static class PopulateRandomBase extends SectionMessage {
        public final Team team;

        public PopulateRandomBase(Team team) {
            this.team = team;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("team", team)
                    .toString();
        }
    }

}
