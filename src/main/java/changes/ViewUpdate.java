package changes;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import dsljson.MapConverters;
import world.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public abstract class ViewUpdate extends Update<WorldCord> {

    public final ViewVersion viewVersion;
    @JsonAttribute(converter = MapConverters.WorldTerrainConverter.class)
    public final Map<WorldCord, Terrain> terrains;
    @JsonAttribute(converter = MapConverters.WorldBaseConverter.class)
    public final Map<WorldCord, Base<WorldCord>> bases;
    @JsonAttribute(converter = MapConverters.WorldUnitConverter.class)
    public final Map<WorldCord, Unit> units;

    ViewUpdate(ViewVersion viewVersion, Map<WorldCord, Terrain> terrains, Map<WorldCord, Base<WorldCord>> bases, Map<WorldCord, Unit> units) {
        this.viewVersion = viewVersion;

        this.terrains = terrains;
        this.bases = bases;
        this.units = units;


    }

    @Override
    protected Map<WorldCord, Terrain> terrainMap() {
        return this.terrains;
    }

    @Override
    protected Map<WorldCord, Base<WorldCord>> baseMap() {
        return this.bases;
    }

    @Override
    protected Map<WorldCord, Unit> unitMap() {
        return this.units;
    }

    public static class Snapshot extends ViewUpdate {


        @CompiledJson
        public Snapshot(ViewVersion viewVersion, Map<WorldCord, Terrain> terrains, Map<WorldCord, Base<WorldCord>> bases, Map<WorldCord, Unit> units) {
            super(viewVersion, terrains, bases, units);
        }

        public static Snapshot create(ViewVersion viewVersion, Map<WorldCord, Terrain> terrains, Map<WorldCord, Base<WorldCord>> bases, Map<WorldCord, Unit> units) {
            return new Snapshot(viewVersion, terrains, bases, units);
        }

        @Override
        public boolean isSnapshot() {
            return true;
        }
    }


    public static class Delta extends ViewUpdate {

        public final Collection<WorldCord> unitDeletes;


        @CompiledJson
        public Delta(ViewVersion viewVersion, Map<WorldCord, Terrain> terrains, Map<WorldCord, Base<WorldCord>> bases, Map<WorldCord, Unit> units, Collection<WorldCord> unitDeletes) {
            super(viewVersion, terrains, bases, units);
            this.unitDeletes = unitDeletes;
        }

        public static Delta create(ViewVersion viewVersion, Map<WorldCord, Unit> units, Collection<WorldCord> unitDeletes) {
            return new Delta(viewVersion, Collections.EMPTY_MAP, Collections.EMPTY_MAP, units, unitDeletes);
        }


        @Override
        public boolean isSnapshot() {
            return false;
        }
    }
}
