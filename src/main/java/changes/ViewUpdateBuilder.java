package changes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.*;

import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class ViewUpdateBuilder extends UpdateBuilder<WorldCord> {
    private static final Logger L = LogManager.getLogger(ViewUpdateBuilder.class);

    private final View view;

    public ViewUpdateBuilder(View view) {
        this.view = view;
    }

    public void add(Integer section, Cord cord, Unit unit) {
        assert view.masterSectionsInView().contains(section);
        this.add(view.sectionToView(section).apply(cord), unit);
    }

    public void add(Integer section, Cord cord, Terrain terrain) {
        assert view.masterSectionsInView().contains(section);
        this.add(view.sectionToView(section).apply(cord), terrain);
    }

    public void removeUnit(Integer section, Cord cord) {
        assert view.masterSectionsInView().contains(section);
        this.removeUnit(view.sectionToView(section).apply(cord));
    }

    /**
     * ToDo
     *
     * @param section
     * @param cord
     * @param base
     */
    public void add(Integer section, Cord cord, Base<Cord> base) {
        assert view.masterSectionsInView().contains(section);
        var tf = view.sectionToView(section);

        L.debug("Adding base:[{}] at section:[{}] section-cord:[{}] world-cord:[{}]", base.name, section, cord, tf.apply(cord));


        var translated = new Base<WorldCord>(
                base.name,
                tf.apply(base.center),
                base.radius,
                base.area.stream().map(tf).collect(toImmutableSet()),
                base.zoneOfControl.stream().map(tf).collect(toImmutableSet())
        );
        this.add(tf.apply(cord), translated);
    }

    public ViewUpdate.Snapshot buildSnapshot(ViewVersion version) {
        return ViewUpdate.Snapshot.create(version, buildMap(Terrain.class), buildBaseMap(), buildMap(Unit.class));
    }

    public ViewUpdate.Delta buildDelta(ViewVersion version) {
        return ViewUpdate.Delta.create(version, buildMap(Unit.class), removedUnits());
    }

    public void addSectionSnapshot(SectionUpdate.Snapshot sectionSnapshot) {
        addSection(sectionSnapshot, makeFilter(sectionSnapshot.sectionVersion.section));
    }

    public void addSectionDelta(SectionUpdate.Delta sectionUpdate) {
        Integer sectionNo = sectionUpdate.sectionVersion.section;
        var filterCord = makeFilter(sectionNo);
        addSection(sectionUpdate, filterCord);

        sectionUpdate.unitDeletes.stream().filter(filterCord).forEach(e -> removeUnit(sectionNo, e));

    }

    public void addSection(SectionUpdate sectionUpdate, Predicate<Cord> filterCord) {
        Integer sectionNo = sectionUpdate.sectionVersion.section;

        Predicate<Map.Entry<Cord, ?>> filterEntry = e -> filterCord.test(e.getKey());
        sectionUpdate.units.entrySet().stream().filter(filterEntry).forEach(e -> add(sectionNo, e.getKey(), e.getValue()));
        sectionUpdate.terrains.entrySet().stream().filter(filterEntry).forEach(e -> add(sectionNo, e.getKey(), e.getValue()));
        sectionUpdate.bases.entrySet().stream().filter(filterEntry).forEach(e -> add(sectionNo, e.getKey(), e.getValue()));


    }

    private Predicate<Cord> makeFilter(Integer sectionNo) {
        return view.isSectionMaster(sectionNo).and(view.isSectionCordInView(sectionNo));

    }


}
