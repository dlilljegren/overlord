package world.exceptions;

import world.*;

import static java.lang.String.format;

public abstract class AddUnitException extends SectionException {

    public final Cord cord;

    protected AddUnitException(int sectionNo, Cord cord, String message) {
        super(sectionNo, message);
        this.cord = cord;
    }


    public static class OccupiedByTerrainException extends AddUnitException {

        public final Terrain terrain;

        public OccupiedByTerrainException(int sectionNo, Cord cord, Terrain terrain) {
            super(sectionNo, cord, format("Cord %s in section:%s is already occupied by terrain [%s]", cord, sectionNo, terrain));
            this.terrain = terrain;
        }
    }

    public static class NotInZocException extends AddUnitException {

        public NotInZocException(int sectionNo, Cord cord, Team team, Team bestTeam) {
            super(sectionNo, cord, format("Team:[%s] does not have enough zoc to add unit at cord [%s] in section:[%s], cell is currently controlled by team [%s]", team, cord, sectionNo, bestTeam));
        }

        public NotInZocException(int sectionNo, Cord cord, Team team) {
            super(sectionNo, cord, format("Cord %s in section:%s in not in controlled by team %s", cord, sectionNo, team));
        }
    }

    public static class OccupiedByBaseException extends AddUnitException {
        public OccupiedByBaseException(int sectionNo, Cord cord, Base base) {
            super(sectionNo, cord, format("Cord %s in section:%s is already occupied by a base [%s]", cord, sectionNo, base));
        }
    }

    public static class OccupiedByUnitException extends AddUnitException {


        public OccupiedByUnitException(int sectionNo, Cord cord, Unit unit) {
            super(sectionNo, cord, format("Cord %s in section:%s is already occupied by unit [%s]", cord, sectionNo, unit));
        }


    }
}
