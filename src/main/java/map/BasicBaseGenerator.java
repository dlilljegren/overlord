package map;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.Base;
import world.Cord;
import world.ITerrainMap;
import world.WorldDefinition;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BasicBaseGenerator implements IBaseGenerator {

    private static final Logger L = LogManager.getLogger(BasicBaseGenerator.class);


    private final WorldDefinition worldDefinition;
    private final int noOfBasesPerSection;
    private final int baseRadius;

    private final Function<Cord, Predicate<Cord>> baseGeometryFactory;

    public BasicBaseGenerator(WorldDefinition worldDefinition, int noOfBasesPerSection, int baseRadius) {
        this.worldDefinition = worldDefinition;
        this.noOfBasesPerSection = noOfBasesPerSection;

        this.baseGeometryFactory = c -> Base.baseGeometry(worldDefinition.sectionDefinition, c);
        this.baseRadius = baseRadius;
    }

    public ImmutableList<Base<Cord>> generateForSection(ITerrainMap<Cord> sectionMap) {
        var generator = new Generator(sectionMap);
        generator.place(noOfBasesPerSection);
        return generator.collect.build();
    }


    private class Generator {
        private final ITerrainMap<Cord> map;
        private final ImmutableList.Builder<Base<Cord>> collect = new ImmutableList.Builder<>();

        private Generator(ITerrainMap<Cord> map) {
            this.map = map;
        }

        private void place(int noOfBases) {
            assert noOfBases >= 0;
            if (noOfBases == 0) {
                return;
            } else if (noOfBases == 1) {
                placeCentre().ifPresent(collect::add);
            } else if (noOfBases == 2) {
                int y = (int) (worldDefinition.sectionHeight() * 0.75 * 0.5);
                place(0, y).ifPresent(collect::add);
                place(0, -y).ifPresent(collect::add);
            } else if (noOfBases <= 4) {
                int x = (int) (worldDefinition.sectionWidth() * 0.75 * 0.5);
                place(2);
                place(x, 0).ifPresent(collect::add);
                place(-x, 0).ifPresent(collect::add);
            } else if (noOfBases == 5) {
                placeCentre().ifPresent(collect::add);
                place(4);
            } else {
                throw new UnsupportedOperationException("Don't know how to place more than 5 bases was:" + noOfBases);
            }
        }

        private Optional<Base> placeCentre() {
            return place(0, 0);
        }

        private Optional<Base> place(int col, int row) {
            Cord baseCentre = Cord.at(col, row);
            assert worldDefinition.section.inSection(baseCentre);
            var p = baseGeometryFactory.apply(baseCentre);
            if (isFree(p)) {
                var base = Base.createInSection(baseCentre, baseRadius, worldDefinition);
                return Optional.of(base);
            } else {
                L.error("ToDo failed to place base, the center will not hold");
                return Optional.empty();
            }
        }


        private boolean isFree(Predicate<Cord> c) {
            return !map.isOccupied(compose(c, x -> (Cord) x));
        }


    }

    public static <E, S> Predicate<E> compose(Predicate<? super S> predicate, Function<E, S> function) {
        return value -> predicate.test(function.apply(value));
    }
}
