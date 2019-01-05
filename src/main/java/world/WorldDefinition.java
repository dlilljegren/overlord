package world;


import com.google.common.collect.Maps;
import games.Rules;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import util.GSColl;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import static java.lang.String.format;
import static org.eclipse.collections.impl.block.factory.Predicates.*;


public class WorldDefinition {

    final static Random random = new Random();
    public final int gridWidth;
    public final int gridHeight;


    public final SectionDefinition sectionDefinition;
    public final int colSectionOverlap;
    public final int rowSectionOverlap;

    public final long mapSeed;

    public final Rules rules;

    public final int worldWidth;
    public final int worldHeight;

    private final PredicateHelper bounds = new PredicateHelper();
    public final SectionTranslations section;
    public final WorldTranslations world;

    public WorldDefinition(
            int gridWidth,
            int gridHeight,
            int xSectionOverlap,
            int ySectionOverlap,
            long mapSeed,
            Rules rules,
            SectionDefinition sectionDefinition
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.colSectionOverlap = xSectionOverlap;
        this.rowSectionOverlap = ySectionOverlap;
        this.sectionDefinition = sectionDefinition;
        this.rules = rules;
        this.mapSeed = mapSeed;

        //Number of columns and rows in the whole world
        this.worldWidth = gridWidth * (sectionWidth() - colSectionOverlap) + colSectionOverlap;
        this.worldHeight = gridHeight * (sectionHeight() - rowSectionOverlap) + rowSectionOverlap;

        this.section = new SectionTranslations(sectionWidth(), sectionHeight());
        this.world = new WorldTranslations();
    }


    public View createViewAtUpperLeft(int sectionNo, int width, int height) {
        var anchor = this.section.worldOrigo(sectionNo);
        var vd = new ViewDefinition(anchor, sectionNo, width, height);
        return new View(this, vd);
    }


    public int randomSectionNo() {
        return random.nextInt(gridWidth * gridHeight);
    }

    public int totalNoOfSectors() {
        return gridWidth * gridHeight;
    }

    public int translateColumn(int fromSection, int toSection) {
        int from = gridColumn(fromSection);
        int to = gridColumn(toSection);
        return (from - to) * (sectionDefinition.cols - colSectionOverlap);
    }

    public int translateRow(int fromSection, int toSection) {
        int from = gridRow(fromSection);
        int to = gridRow(toSection);
        return (from - to) * (sectionDefinition.rows - rowSectionOverlap);
    }


    public boolean isValid(WorldCord cord) {
        return bounds.columnIsInsideWorld.test(cord) && bounds.rowIsInsideWorld.test(cord);
    }

    public int worldTotalColumns() {
        return this.worldWidth;
    }

    public int worldTotalRows() {
        return this.worldHeight;
    }


    public Function<WorldCord, Cord> toSection(int section) {
        return wc -> fromWorldToSection(wc, section);
    }

    public Cord fromWorldToSection(WorldCord world, int section) {
        return Cord.at(fromWorldToSectionX(world.col, section), fromWorldToSectionY(world.row, section));
    }


    public int fromWorldToSectionX(int worldX, int toSection) {
        var gridColumn = gridColumn(toSection);
        //sectionSize * gridColumn + sectionX = worldX if we had no overlap
        //sectionSize * gridColumn - overlapX *gridColumn +sectionX= worldX
        //gridColumn * ( sectionSize -overlap) + sectionX = worldX
        return worldX - gridColumn * (sectionWidth() - colSectionOverlap) - sectionWidth() / 2;
    }


    public int fromWorldToSectionY(int worldY, int toSection) {
        var gridRow = gridRow(toSection);
        return worldY - gridRow * (sectionHeight() - rowSectionOverlap) - sectionHeight() / 2;
    }


    public int sectionWidth() {
        return sectionDefinition.cols;
    }

    public int sectionHeight() {
        return sectionDefinition.rows;
    }


    public int gridColumn(int sectionNo) {
        return sectionNo % this.gridWidth;
    }

    public int gridRow(int sectionNo) {
        return sectionNo / this.gridWidth;
    }


    public int masterSectionAt(int gridColumn, int gridRow) {
        return gridRow * gridWidth + gridColumn;
    }


    public Optional<Integer> northWestSection(int sectionNo) {
        return Optional.of(sectionNo).filter(this::valid).flatMap(this::westSection).flatMap(this::northSection);
    }


    public Optional<Integer> northSection(int sectionNo) {
        int row = gridRow(sectionNo) - 1;
        if (row < 0) return Optional.empty();
        int col = gridColumn(sectionNo);
        return Optional.of(masterSectionAt(col, row)).filter(this::valid);
    }

    private boolean valid(Integer sectionNo) {
        return sectionNo >= 0 && sectionNo < totalNoOfSectors();
    }

    public Optional<Integer> westSection(int sectionNo) {
        int column = gridColumn(sectionNo) - 1;

        if (column < 0) return Optional.empty();
        int row = gridRow(sectionNo);
        return Optional.of(masterSectionAt(column, row)).filter(this::valid);
    }



    /**
     * Creates the geometry for the rectangle where this sectionNo is master
     *
     * @param sectionNo
     * @return
     */
    @Deprecated
    Predicate<Cord> masterRectangle(int sectionNo) {
        boolean fn = section.isNorthernBorder(sectionNo);
        boolean fw = section.isWesternBorder(sectionNo);

        if (fn && fw) {
            return this.sectionDefinition.inSection;
        }
        if (fn) {
            return and(not(bounds.insideWestStrip), sectionDefinition::inSection);//If north we are master as long as not in west strip
        }
        if (fw) {
            return and(not(bounds.insideNorthStrip), sectionDefinition::inSection);//If north we are master as long as not in north strip
        }
        return and(bounds.outsideWestAndNorth, sectionDefinition::inSection);
    }


    class PredicateHelper {
        private final Predicate<Cord> insideNorthStrip = sectionCord -> section.midToUpperLeftCorner.apply(sectionCord).row < rowSectionOverlap;
        private final Predicate<Cord> insideWestStrip = sectionCord -> section.midToUpperLeftCorner.apply(sectionCord).col < colSectionOverlap;

        private final Predicate<Cord> outsideWestAndNorth = or(insideNorthStrip, insideWestStrip).not();

        private final Predicate<WorldCord> columnIsInsideWorld = wc -> wc.col < worldWidth && wc.col >= 0;
        private final Predicate<WorldCord> rowIsInsideWorld = wc -> wc.row < worldHeight && wc.row >= 0;
    }

    public class SectionTranslations {
        private final Function<Cord, Cord> midToUpperLeftCorner;
        private final Function<Cord, Cord> upperLeftCornerToMid;
        private final int hsw;
        private final int hsh;

        private final ConcurrentMap<Integer, MasterRectangle> masterRectangleCache;

        private final IntObjectMap<ImmutableSet<Cord>> radiusToCentralCircle;

        public final Predicate<Integer> isValidSectionNo = sectionNo -> sectionNo >= 0 && sectionNo < world.totalSections;

        /**
         * Transform a cord into an array index for faster lookups compared to Map
         */
        public final ToIntFunction<Cord> toIndex;

        public final java.util.function.IntFunction<Cord> toCord;


        private SectionTranslations(int sectionWidth, int sectionHeight) {
            this.hsw = sectionWidth / 2;
            this.hsh = sectionHeight / 2;

            midToUpperLeftCorner = mid -> Cord.at(
                    mid.col + hsw, mid.row + hsh
            );

            upperLeftCornerToMid = ul -> Cord.at(
                    ul.col - hsw, ul.row - hsh
            );

            this.masterRectangleCache = Maps.newConcurrentMap();

            this.toIndex = c -> c.col + c.row * sectionWidth;
            this.toCord = index -> Cord.at(index % sectionWidth, index / sectionWidth);

            this.radiusToCentralCircle = IntObjectMaps.mutable.empty();
        }

        public WorldCord worldOrigo(int sectionNo) {
            return world.sectionOrigo.apply(sectionNo);
        }

        public WorldCord worldCenter(int sectionNo) {
            return worldOrigo(sectionNo).add(hsw, hsh);
        }


        public ImmutableSet<Cord> circle(int radius) {

            return this.radiusToCentralCircle.getIfAbsent(radius, () -> sectionDefinition.cordsInCircle(Cord.at(0, 0), radius).collect(GSColl.immutableSet()));

        }

        /**
         * @param sectionNo
         * @return function that for a given mainSection converts to world coordinates
         */
        public Function<Cord, WorldCord> toWorld(int sectionNo) {
            WorldCord origo = world.sectionOrigo.apply(sectionNo);
            Function<Cord, WorldCord> add = c -> {
                var wc = origo.add(c.col, c.row);
                assert isValid(wc) : format("WorldCord:[%s] is not valid, was converting cord:[%s] in section:[%d]", wc, c, sectionNo);
                return wc;
            };
            java.util.function.Function<Cord, WorldCord> res = midToUpperLeftCorner.andThen(add);

            return c -> res.apply(c);
        }

        /**
         * @return function translating to the sections default mid coordinate system where the center cell has coordinate 0:0
         */
        public Function<Cord, Cord> toMid() {
            return upperLeftCornerToMid;
        }


        public boolean inSection(Cord cord) {
            return sectionDefinition.inSection(cord);
        }

        public Predicate<Cord> isSectionMaster(Integer sectionNo) {
            var mr = masterRectangle(sectionNo);
            return c -> mr.insideMasterRect(c);
        }



        public MasterRectangle masterRectangle(Integer sectionNo) {
            return masterRectangleCache.computeIfAbsent(sectionNo, section -> new MasterRectangle(section));
        }

        /**
         * @param sectionNo
         * @return true if this section is at the southern border
         */
        public boolean isWesternBorder(int sectionNo) {
            return gridColumn(sectionNo) == 0;
        }

        /**
         * @param sectionNo
         * @return true if this section is at the northern border
         */
        public boolean isNorthernBorder(int sectionNo) {
            return gridRow(sectionNo) == 0;
        }

        public class MasterRectangle {
            public final Cord NE;
            public final Cord NW;
            public final Cord SE;
            public final Cord SW;

            MasterRectangle(int sectionNo) {
                var nw = upperLeftCornerToMid.apply(Cord.at(0, 0));//Move to mid coordinate system
                var offsetCol = section.isWesternBorder(sectionNo) ? 0 : colSectionOverlap;
                var offsetRow = section.isNorthernBorder(sectionNo) ? 0 : rowSectionOverlap;
                var masterWidth = sectionWidth() - offsetCol - 1;//-1 to keep them inside i.e <=
                var masterHeight = sectionHeight() - offsetRow - 1;

                NW = nw.add(offsetCol, offsetRow);
                NE = NW.add(masterWidth, 0);
                SW = NW.add(0, masterHeight);
                SE = SW.add(masterWidth, 0);
            }

            boolean insideMasterRect(Cord c) {
                return c.col >= NW.col && c.col <= NE.col && c.row >= NW.row && c.row <= SW.row;
            }
        }
    }

    public class WorldTranslations {
        private final java.util.function.IntFunction<WorldCord> sectionOrigo;
        private final BiFunction<Cord, Integer, WorldCord> fromSectionToWorld;
        public final Integer totalSections = gridWidth * gridHeight;

        private WorldTranslations() {
            int sw = sectionWidth() - colSectionOverlap;
            int sh = sectionHeight() - rowSectionOverlap;

            sectionOrigo = sectionNo -> WorldCord.at(WorldDefinition.this.gridColumn(sectionNo) * sw, WorldDefinition.this.gridRow(sectionNo) * sh);


            fromSectionToWorld = (cord, sectionNo) -> {
                var c = section.midToUpperLeftCorner.apply(cord);
                return sectionOrigo.apply(sectionNo).add(c.col, c.row);
            };
        }

        public Function<WorldCord, Cord> toSection(int sectionNo) {
            WorldCord origo = world.sectionOrigo.apply(sectionNo);

            return wc -> {
                //First calculate where we land in the mainSection once removing origo
                var inSectionUpperLeftSystem = Cord.at(wc.col - origo.col, wc.row - origo.row);
                //Then adjust for the mid-system
                return section.upperLeftCornerToMid.apply(inSectionUpperLeftSystem);

            };
        }

        public Predicate<WorldCord> isSectionMaster(int sectionNo) {
            var first = this.toSection(sectionNo);
            var p = section.isSectionMaster(sectionNo);
            return wc -> p.accept(first.apply(wc));
        }

        /**
         * Transform the cord into the section grid coordinate, then find the master section at that grid coordinate
         *
         * @param wc
         * @return the mainSection id for the world cord
         */
        public int masterSectionAt(WorldCord wc) {
            assert isValid(wc) : wc;

            int gridColumn = gridColumn(wc);
            if (gridColumn < 0) gridColumn = 0;

            int gridRow = gridRow(wc);
            if (gridRow < 0) gridRow = 0;

            return WorldDefinition.this.masterSectionAt(gridColumn, gridRow);
        }

        public int gridColumn(WorldCord wc) {
            return (wc.col - colSectionOverlap) / (sectionWidth() - colSectionOverlap);
        }

        public int gridColumnBounded(WorldCord wc) {
            int gc = gridColumn(wc);
            if (gc < 0) return 0;
            if (gc > gridWidth) return gridWidth;
            return gc;
        }

        public int gridRowBounded(WorldCord wc) {
            int gr = gridRow(wc);
            if (gr < 0) return 0;
            if (gr > gridHeight) return gridHeight;
            return gr;
        }

        public int gridRow(WorldCord wc) {
            return (wc.row - rowSectionOverlap) / (sectionHeight() - rowSectionOverlap);
        }
    }
}
