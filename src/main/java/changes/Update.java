package changes;

import com.google.common.base.MoreObjects;
import world.Base;
import world.ICord;
import world.Terrain;
import world.Unit;

import java.util.Map;

public abstract class Update<C extends ICord> {


    public abstract boolean isSnapshot();


    abstract protected Map<C, Terrain> terrainMap();

    protected abstract Map<C, Base<C>> baseMap();

    protected abstract Map<C, Unit> unitMap();

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("isSnapshot", isSnapshot())
                //.add("width", width())
                //.add("height", height())
                .add("terrains", new TerrainsInfo())
                .add("units", unitMap().size())
                .add("bases", new BaseInfo())
                .toString();
    }

    class BaseInfo {
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("bases", baseMap().entrySet())
                    .toString();
        }

    }

    class TerrainsInfo {
        final int hills;
        final int mountains;
        final int water;

        TerrainsInfo() {
            var map = terrainMap();
            hills = (int) map.values().stream().filter(t -> t == Terrain.Hill).count();
            mountains = (int) map.values().stream().filter(t -> t == Terrain.Mountain).count();
            water = (int) map.values().stream().filter(t -> t == Terrain.Water).count();
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
