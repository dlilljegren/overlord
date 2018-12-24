package world;

import com.google.common.collect.Maps;
import gnu.trove.map.hash.TObjectIntHashMap;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.function.ToIntFunction;

public class ZoneOfControlCalculator2 implements IZoneOfControlCalculator {
    private final int section;
    @Nonnull
    private final WorldDefinition worldDefinition;

    private final SectionDefinition sectionDefinition;

    private final CellControl2[] points;

    private final ToIntFunction<Cord> hash;
    private final int zocRadius;

    public ZoneOfControlCalculator2(int section, @Nonnull WorldDefinition worldDefinition) {
        this.section = section;
        this.worldDefinition = worldDefinition;
        this.sectionDefinition = worldDefinition.sectionDefinition;
        this.points = new CellControl2[worldDefinition.sectionDefinition.area()];

        this.hash = worldDefinition.section.cordHash;

        this.zocRadius = worldDefinition.rules.zocRadius();
    }


    private CellControl2 at(Cord cord) {
        int index = hash.applyAsInt(cord);
        var result = points[index];
        if (result == null) {
            result = points[index] = new CellControl2();
        }
        return result;
    }

    private void clearAll() {
        for (var cc : points) {
            if (cc != null) {
                cc.clear();
            }
        }
    }


    @Override
    public ZocResultSnapshot calculateSnapshot(Map<Cord, Unit> units) {
        clearAll();
        units.forEach(this::influence);
    }


    @Override
    public ZocResultDelta calculateDelta(Map<Cord, Unit> units, Collection<Cord> removed) {
        return null;
    }

    private void influence(Cord unitCord, Unit unit) {
        //This cord influences it's neighbours
        worldDefinition.sectionDefinition.cordsInCircle(unitCord, zocRadius).forEach(
                c -> at(c).influence(c, unitCord, unit)
        );
    }

    private class CellControl2 {
        TObjectIntHashMap<Team> teamInfluence = new TObjectIntHashMap<>(3);
        Map<Cord, Unit> influencers = Maps.newHashMap();

        private void influence(Cord me, Cord unitCord, Unit unit) {
            int points = 1 + zocRadius - sectionDefinition.distance(unitCord, me);
            assert points >= 0;
            teamInfluence.adjustOrPutValue(unit.team, points, points);
            influencers.put(unitCord, unit);
        }

        private void clear() {
            teamInfluence.clear();
            influencers.clear();
        }
    }
}
