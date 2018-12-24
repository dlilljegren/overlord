package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ITerrainMap<C extends ICord> {
    ITerrainMap EMPTY = new TerrainMap(Collections.EMPTY_MAP);


    //int width();
    //int height();

    Map<C, Terrain> terrainMap();

    default Stream<Terrain> terrains() {
        return terrainMap().values().stream();
    }

    default String mapInfo() {
        return MoreObjects.toStringHelper(this)
                //.add("width", width())
                //.add("height", height())
                .add("terrains", new TerrainsInfo(this))
                .toString();
    }

    default boolean isOccupied(Predicate<C> area) {
        return terrainMap().keySet().stream().anyMatch(area);
    }


    static <C extends ICord> ITerrainMap<C> create(Map<C, Terrain> map) {
        return new TerrainMap<>(map);
    }


    class TerrainMap<C extends ICord> implements ITerrainMap<C> {
        public final Map<C, Terrain> map;

        public TerrainMap(Map<C, Terrain> map) {
            this.map = map;
        }

        @Override
        public Map<C, Terrain> terrainMap() {
            return map;
        }
    }

    @CompiledJson
    class SectionTerrainMap implements ITerrainMap<Cord> {
        public final Map<Cord, Terrain> map;

        SectionTerrainMap(final Map<Cord, Terrain> map) {
            this.map = map;
        }

        @Override
        public Map<Cord, Terrain> terrainMap() {
            return map;
        }
    }


    class TerrainsInfo {
        final int hills;
        final int mountains;
        final int water;

        TerrainsInfo(ITerrainMap<?> map) {
            hills = (int) map.terrains().filter(t -> t == Terrain.Hill).count();
            mountains = (int) map.terrains().filter(t -> t == Terrain.Mountain).count();
            water = (int) map.terrains().filter(t -> t == Terrain.Water).count();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("hills", hills)
                    .add("mountains", mountains)
                    .add("water", water)
                    .toString();
        }
    }
}
