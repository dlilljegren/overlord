package world;

import com.google.common.collect.Sets;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import java.util.Set;


public class View {
    private final WorldDefinition worldDefinition;
    private final ViewDefinition viewDefinition;


    private final Function<WorldCord, WorldCord> worldToView;


    private final Function<WorldCord, WorldCord> viewToWorld;


    private final Predicate<WorldCord> inView;

    private final MutableIntObjectMap<Function<Cord, WorldCord>> sectionToViewFuncCache;
    private final MutableIntObjectMap<Function<WorldCord, Cord>> viewToSectionFuncCache;
    private final MutableIntObjectMap<Predicate<Cord>> isSectionCordInViewPredicateCache;
    private final MutableIntObjectMap<MasterRectangle> masterRectCache;



    public View(WorldDefinition worldDefinition, ViewDefinition viewDefinition) {
        this.worldDefinition = worldDefinition;
        this.viewDefinition = viewDefinition;

        int minCol = 0;
        int maxCol = viewDefinition.width;
        int minRow = 0;
        int maxRow = viewDefinition.height;

        final var origo = viewDefinition.origo;
        this.worldToView = wc -> wc.add(-origo.col, -origo.row);
        this.viewToWorld = wc -> wc.add(origo.col, origo.row);

        this.inView = viewCord -> viewCord.col >= minCol && viewCord.col < maxCol && viewCord.row >= minRow && viewCord.row < maxRow;

        this.sectionToViewFuncCache = IntObjectMaps.mutable.empty();
        this.viewToSectionFuncCache = IntObjectMaps.mutable.empty();
        this.masterRectCache = IntObjectMaps.mutable.empty();

        isSectionCordInViewPredicateCache = IntObjectMaps.mutable.empty();

    }


    public static View create(final WorldDefinition worldDefinition, final int sectionNo, final int width, final int height) {
        var wc = worldDefinition.section.worldOrigo(sectionNo);//.add(-width/2, -height/2); //We want origo in the middle
        return new View(worldDefinition, new ViewDefinition(wc, sectionNo, width, height));
    }

    /**
     * @return the id of all sections which main area is within the view
     */
    public Set<Integer> masterSectionsInView() {
        //We determine the min grid column and the max grid column loop over them and get master
        var diagonal = viewDefinition.origo.add(viewDefinition.width - 1, viewDefinition.height - 1);

        var minCol = worldDefinition.world.gridColumnBounded(viewDefinition.origo);
        var minRow = worldDefinition.world.gridRowBounded(viewDefinition.origo);
        var maxCol = worldDefinition.world.gridColumnBounded(diagonal);
        var maxRow = worldDefinition.world.gridRowBounded(diagonal);


        Set<Integer> result = Sets.newTreeSet();
        for (var c = minCol; c <= maxCol; c++) {
            for (var r = minRow; r <= maxRow; r++) {
                Integer next = worldDefinition.masterSectionAt(c, r);
                if (worldDefinition.section.isValidSectionNo.test(next)) {
                    assert !result.contains(next);
                    result.add(next);
                }
            }
        }

        assert result.stream().allMatch(worldDefinition.section.isValidSectionNo) : "Returned an non valid sectionNo " + result.stream().filter(worldDefinition.section.isValidSectionNo.negate()).findFirst().get();

        return result;
    }

    public Function<Cord, WorldCord> sectionToView(int sectionNo) {
        return sectionToViewFuncCache.getIfAbsentPutWithKey(sectionNo, sn -> Functions.chain(worldDefinition.section.toWorld(sn), worldToView));
    }


    public Function<WorldCord, Cord> viewToSection(int sectionNo) {
        return viewToSectionFuncCache.getIfAbsentPutWithKey(sectionNo, sn -> Functions.chain(viewToWorld, worldDefinition.world.toSection(sn)));
    }

    WorldCord viewToWorld(WorldCord viewCord) {
        return viewToWorld.apply(viewCord);
    }

    WorldCord worldToView(WorldCord worldCord) {
        return worldToView.apply(worldCord);
    }


    /**
     * @param viewCord
     * @return Given a coordinate in the view return the mainSection in charge
     */
    public int masterSection(WorldCord viewCord) {
        return worldDefinition.world.masterSectionAt(viewToWorld.apply(viewCord));
    }

    /**
     * Is in the view using the views coordinate system
     *
     * @return
     */
    public Predicate<WorldCord> inViewLocal() {
        return inView;
    }

    public Predicate<Cord> isSectionCordInView(int sectionNo) {
        return isSectionCordInViewPredicateCache.getIfAbsentPutWithKey(sectionNo, sn -> Predicates.attributePredicate(sectionToView(sectionNo), inView));
    }

    public Predicate<Cord> isSectionMaster(Integer sectionNo) {
        return worldDefinition.section.isSectionMaster(sectionNo);
    }

    public MasterRectangle masterRectangle(int sectionNo) {
        return masterRectCache.getIfAbsentPutWithKey(sectionNo, sn -> new MasterRectangle(sn));
    }

    public class MasterRectangle {

        final WorldCord NW;
        final WorldCord NE;
        final WorldCord SW;
        final WorldCord SE;

        MasterRectangle(int sectionNo) {
            var srm = worldDefinition.section.masterRectangle(sectionNo);
            NW = sectionToView(sectionNo).apply(srm.NW);
            NE = sectionToView(sectionNo).apply(srm.NE);
            SW = sectionToView(sectionNo).apply(srm.SW);
            SE = sectionToView(sectionNo).apply(srm.SE);
        }

        public Rect asRect() {
            return new Rect(NW.col, NW.row, NE.col - NW.col, SW.row - NW.row);
        }
    }
}
