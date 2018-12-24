package world;

import changes.SectionUpdate;

public class SectorCombatResult {

    public final SectionUpdate.Delta delta;


    public final SectorCombatStats combatStats;

    public SectorCombatResult(SectionUpdate.Delta delta, SectorCombatStats combatStats) {
        assert !delta.isSnapshot();
        this.delta = delta;
        this.combatStats = combatStats;
    }
}
