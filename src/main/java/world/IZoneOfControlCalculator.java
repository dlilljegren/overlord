package world;

import java.util.Map;

/**
 * Calculates the current zone of controls changes for a section
 */
public interface IZoneOfControlCalculator {

    /**
     * Calculate the coordinates where each team can place new units, the result only contains cords where we are master
     *
     * @return
     */
    ZocResult getSnapshot();

    ZocResult getDelta();


    void addUnit(Unit unit, Cord at);

    void removeUnit(Unit unit, Cord at);

    void addUnits(Map<Cord, Unit> units);

    void removeUnits(Map<Cord, Unit> units);


    boolean hasControl(Team team, Cord cord);
}
