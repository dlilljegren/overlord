package world;

import com.dslplatform.json.CompiledJson;

import java.util.Collections;
import java.util.Map;

public interface IUnitMap<C extends ICord> {

    IUnitMap EMPTY = new UnitMap(Collections.EMPTY_MAP);


    Map<C, Unit> unitMap();


    class UnitMap<C extends ICord> implements IUnitMap<C> {
        public final Map<C, Unit> map;

        UnitMap(final Map<C, Unit> map) {
            this.map = map;
        }

        @Override
        public Map<C, Unit> unitMap() {
            return map;
        }
    }

    @CompiledJson
    class WorldUnitMap implements IUnitMap<WorldCord> {
        public final Map<WorldCord, Unit> map;

        WorldUnitMap(final Map<WorldCord, Unit> map) {
            this.map = map;
        }

        @Override
        public Map<WorldCord, Unit> unitMap() {
            return map;
        }
    }

    @CompiledJson
    class SectionUnitMap implements IUnitMap<Cord> {
        public final Map<Cord, Unit> map;

        SectionUnitMap(final Map<Cord, Unit> map) {
            this.map = map;
        }

        @Override
        public Map<Cord, Unit> unitMap() {
            return map;
        }
    }
}
