package world;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class View {
    private final WorldDefinition worldDefinition;
    private final ViewDefinition viewDefinition;


    private final Function<WorldCord, WorldCord> worldToView;


    private final Function<WorldCord, WorldCord> viewToWorld;


    private final Predicate<WorldCord> inView;

    private final Map<Integer, Function<Cord, WorldCord>> sectionToViewFuncCache;
    private Map<Integer, Predicate<Cord>> isSectionCordInViewPredicateCache;
    private Map<Integer, MasterRectangle> masterRectCache;


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

        this.sectionToViewFuncCache = Maps.newHashMap();
        this.masterRectCache = Maps.newHashMap();

        isSectionCordInViewPredicateCache = Maps.newHashMap();
    }


    public static View create(final WorldDefinition worldDefinition, final int sectionNo, final int width, final int height) {
        var wc = worldDefinition.section.worldOrigo(sectionNo);//.add(-width/2, -height/2); //We want origo in the middle
        return new View(worldDefinition, new ViewDefinition(wc, sectionNo, width, height));
    }

    /**
     * @return all mainSection id's inside this view
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
                var next = worldDefinition.masterSectionAt(c, r);
                assert !result.contains(next);
                result.add(next);
            }
        }

        assert result.stream().allMatch(worldDefinition.section.isValidSectionNo) : "Returned an non valid sectionNo " + result.stream().filter(worldDefinition.section.isValidSectionNo.negate()).findFirst().get();

        return result;
    }

    public Function<Cord, WorldCord> sectionToView(Integer sectionNo) {
        var f = sectionToViewFuncCache.get(sectionNo);
        if (f == null) {
            f = worldDefinition.section.toWorld(sectionNo).andThen(worldToView);//Function from mainSection to world, the word to view
            sectionToViewFuncCache.put(sectionNo, f);
        }
        return f;
    }


    public Function<WorldCord, Cord> viewToSection(Integer sectionNo) {
        var worldToSection = worldDefinition.world.toSection(sectionNo);//Function from world to mainSection
        return worldToSection.compose(viewToWorld);//Then mainSection to the view
    }

    public WorldCord viewToWorld(WorldCord viewCord) {
        return viewToWorld.apply(viewCord);
    }

    public WorldCord worldToView(WorldCord worldCord) {
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

    public Predicate<Cord> isSectionCordInView(Integer section) {
        var f = isSectionCordInViewPredicateCache.get(section);
        if (f == null) {
            var sectionToView = sectionToView(section);
            f = sc -> inView.test(sectionToView.apply(sc));
            isSectionCordInViewPredicateCache.put(section, f);
        }
        return f;

    }

    public Predicate<Cord> isSectionMaster(Integer sectionNo) {
        return worldDefinition.section.isSectionMaster(sectionNo);
    }

    public MasterRectangle masterRectangle(Integer sectionNo) {
        var f = masterRectCache.get(sectionNo);
        if (f == null) {
            f = new MasterRectangle(sectionNo);
            masterRectCache.put(sectionNo, f);
        }
        return f;
    }

    public class MasterRectangle {

        public final WorldCord NW;
        public final WorldCord NE;
        public final WorldCord SW;
        public final WorldCord SE;

        MasterRectangle(Integer sectionNo) {
            var srm = worldDefinition.section.masterRectangle(sectionNo);
            NW = sectionToView(sectionNo).apply(srm.NW);
            NE = sectionToView(sectionNo).apply(srm.NE);
            SW = sectionToView(sectionNo).apply(srm.SW);
            SE = sectionToView(sectionNo).apply(srm.SE);
        }

    }
}
