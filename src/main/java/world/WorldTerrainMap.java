package world;

import com.dslplatform.json.CompiledJson;

import java.util.Map;

@CompiledJson
public class WorldTerrainMap implements ITerrainMap<WorldCord> {
    public final Map<WorldCord, Terrain> map;

    public WorldTerrainMap(final Map<WorldCord, Terrain> map) {
        this.map = map;
    }

    @Override
    public Map<WorldCord, Terrain> terrainMap() {
        return map;
    }
}
