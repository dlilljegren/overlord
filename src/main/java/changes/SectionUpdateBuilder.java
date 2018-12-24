package changes;

import world.Cord;
import world.SectionVersion;
import world.Terrain;
import world.Unit;

public class SectionUpdateBuilder extends UpdateBuilder<Cord> {


    public SectionUpdateBuilder() {


    }


    public SectionUpdate.Snapshot buildSnapshot(SectionVersion version) {
        return new SectionUpdate.Snapshot(version, buildMap(Terrain.class), buildBaseMap(), buildMap(Unit.class));
    }


    public SectionUpdate.Delta buildDelta(SectionVersion next) {
        return new SectionUpdate.Delta(next, buildMap(Unit.class), removedUnits());
    }


}

