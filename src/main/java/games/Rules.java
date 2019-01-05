package games;

public interface Rules {


    /**
     * Zone of Control point are given to all cells were distance is less than this
     *
     * @return
     */
    default int zocRadius() {
        return 3;
    }

    /**
     * @return number of units that are divided on each turn to the players in the sectionNo
     */
    default Integer unitsAllocatedPerSection() {
        return 20;
    }

    default Integer unitsAllocatedPerBase() {
        return 10;
    }
}
