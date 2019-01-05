package world;

import java.util.Map;

/**
 * Calculates the current zone of controls changes for a section
 */
public interface IZoneOfControlCalculator {

    /**
     * Calculate the coordinates where each team can place new units, the result only contains cords where we are master
     *
     * @param units should pass in all coordinates here including ones where the section is not master
     * @return
     */
    ZocResult calculateSnapshot(Map<Cord, Unit> units);

    ZocResult calculateDelta(Map<Cord, Unit> added, Map<Cord, Unit> removed);


    boolean hasControl(Team team, Cord cord);
}
