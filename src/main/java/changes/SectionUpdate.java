package changes;

import world.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public abstract class SectionUpdate extends Update<Cord> {

    public final Map<Cord, Terrain> terrains;
    public final Map<Cord, Base<Cord>> bases;
    public final Map<Cord, Unit> units;
    public final SectionVersion sectionVersion;


    SectionUpdate(SectionVersion sectionVersion, Map<Cord, Terrain> terrains, Map<Cord, Base<Cord>> bases, Map<Cord, Unit> units) {
        this.terrains = terrains;
        this.bases = bases;
        this.units = units;

        this.sectionVersion = sectionVersion;


    }

    @Override
    protected Map<Cord, Terrain> terrainMap() {
        return this.terrains;
    }

    @Override
    protected Map<Cord, Base<Cord>> baseMap() {
        return this.bases;
    }

    @Override
    protected Map<Cord, Unit> unitMap() {
        return this.units;
    }

    public static class Snapshot extends SectionUpdate {


        Snapshot(SectionVersion sectionVersion, Map<Cord, Terrain> terrains, Map<Cord, Base<Cord>> bases) {
            super(sectionVersion, terrains, bases, Collections.EMPTY_MAP);
        }


        Snapshot(SectionVersion sectionVersion, Map<Cord, Terrain> terrains, Map<Cord, Base<Cord>> bases, Map<Cord, Unit> units) {
            super(sectionVersion, terrains, bases, units);
        }

        @Override
        public boolean isSnapshot() {
            return true;
        }


    }

    public static class Delta extends SectionUpdate {

        public final Collection<Cord> unitDeletes;


        Delta(SectionVersion sectionVersion, Map<Cord, Terrain> terrains, Map<Cord, Base<Cord>> bases, Map<Cord, Unit> units, Collection<Cord> unitDeletes) {
            super(sectionVersion, terrains, bases, units);
            this.unitDeletes = unitDeletes;
        }

        Delta(SectionVersion sectionVersion, Map<Cord, Unit> units, Collection<Cord> unitDeletes) {
            this(sectionVersion, Collections.EMPTY_MAP, Collections.EMPTY_MAP, units, unitDeletes);
        }

        @Override
        public boolean isSnapshot() {
            return false;
        }
    }


}
