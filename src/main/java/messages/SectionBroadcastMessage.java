package messages;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;
import world.Cord;
import world.SectorCombatResult;
import world.Team;
import world.Unit;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public abstract class SectionBroadcastMessage {

    public final int sectionNo;


    protected SectionBroadcastMessage(int sectionNo) {
        assert sectionNo >= 0;
        this.sectionNo = sectionNo;
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class UnitAdded extends SectionBroadcastMessage {

        @Nonnull
        public final Unit unit;
        @Nonnull
        public final Cord cord;

        public UnitAdded(int sectionNo, Unit unit, Cord cord) {
            super(sectionNo);
            assert unit != null;
            assert cord != null;
            this.unit = unit;
            this.cord = cord;
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class UnitRemoved extends SectionBroadcastMessage {
        @Nonnull
        public final Unit unit;
        @Nonnull
        public final Cord cord;

        public UnitRemoved(int sectionNo, Unit unit, Cord cord) {
            super(sectionNo);
            assert unit != null;
            assert cord != null;
            this.unit = unit;
            this.cord = cord;
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class ZoneOfControl extends SectionBroadcastMessage {

        public final Map<Team, Set<Cord>> gained;
        public final Map<Team, Set<Cord>> lost;

        public ZoneOfControl(int sectionNo, Map<Team, Set<Cord>> gained, Map<Team, Set<Cord>> lost) {
            super(sectionNo);

            this.gained = gained;
            this.lost = lost;
        }


        public boolean isEmpty() {
            return this.gained.isEmpty() && this.lost.isEmpty();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sectionNo", sectionNo)
                    .add("gained", format("Teams:%d Cords:%d", gained.size(), gained.values().stream().flatMap(s -> s.stream()).count()))
                    .add("lost", format("Teams:%d Cords:%d", lost.size(), lost.values().stream().flatMap(s -> s.stream()).count()))
                    .toString();
        }
    }



    public static class CombatInfo extends SectionBroadcastMessage {
        public final int combatNo;

        public CombatInfo(int sectionNo, int combatNo) {
            super(sectionNo);
            this.combatNo = combatNo;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sectionNo", sectionNo)
                    .add("combatNo", combatNo)
                    .toString();
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class CombatStarted extends CombatInfo {
        public CombatStarted(int sectionNo, int combatNo) {
            super(sectionNo, combatNo);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class CombatEnded extends CombatInfo {
        public CombatEnded(int sectionNo, int combatNo) {
            super(sectionNo, combatNo);
        }
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class CombatCountDown extends CombatInfo {
        public final int secondsLeft;

        public CombatCountDown(int sectionNo, int combatNo, int secondsLeft) {
            super(sectionNo, combatNo);
            this.secondsLeft = secondsLeft;
        }
    }


    public static class CombatResult extends CombatInfo {


        public final SectorCombatResult combatResult;

        public CombatResult(int sectionNo, int combatNo, SectorCombatResult combatResult) {
            super(sectionNo, combatNo);
            this.combatResult = combatResult;
        }
    }


}
