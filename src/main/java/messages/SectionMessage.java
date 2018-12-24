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

        public static class IllegalTerrain extends Failed {

            private final Terrain terrain;
            private final Cord cord;

            protected IllegalTerrain(int section, String errorMessage, Terrain terrain, Cord cord) {
                super(section, errorMessage);

                this.terrain = terrain;
                this.cord = cord;
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

        public static class Ack extends SectionMessage {
            private final Unit unit;
            private final Cord cord;
            private final Team team;

            public Ack(Unit unit, Cord cord, Team team) {

                this.unit = unit;
                this.cord = cord;
                this.team = team;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("unit", unit)
                        .add("cord", cord)
                        .add("team", team)
                        .toString();
            }
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


}
