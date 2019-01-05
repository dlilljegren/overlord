package world;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Calculate for each cord which team is in control of the cord, Players can only place units on cords under there control
 * ToDo should be made reusable to minimize GC
 */
@Deprecated
class ZoneOfControlCalculator {

    protected final SectionDefinition sectionDefinition;


    private final Map<Cord, Unit> units;

    final NavigableMap<Cord, CellControl> points;


    private boolean hasCalculated = false;

    ZoneOfControlCalculator(SectionDefinition sectionDefinition, Map<Cord, Unit> units, Optional<Predicate<Cord>> unitFilter) {
        this.sectionDefinition = sectionDefinition;
        var scPredicate = unitFilter.orElse(Predicates.alwaysTrue());

        this.units = filterByKey(units, scPredicate);

        this.points = Maps.newTreeMap();
    }


    public static <K, V> Map<K, V> filterByKey(Map<K, V> map, Predicate<K> predicate) {
        return map.entrySet()
                .stream()
                .filter(x -> predicate.test(x.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private void calculate() {
        if (!hasCalculated) {
            units.entrySet().stream()
                    .flatMap(this::process)
                    .forEach(this::assign);
            hasCalculated = true;
        }
    }

    private void assign(TeamPoints teamPoints) {
        CellControl cell = points.computeIfAbsent(teamPoints.cord, CellControl::new);
        cell.add(teamPoints.team, teamPoints.points);
    }


    Stream<CellControl> result() {
        calculate();
        return points.values().stream();
    }

    /**
     * Expand the around the unit, creating small helpers
     *
     * @return
     */
    protected Stream<TeamPoints> process(Map.Entry<Cord, Unit> unitAtCord) {
        Cord center = unitAtCord.getKey();
        Team team = unitAtCord.getValue().team;
        return sectionDefinition.neighbours(center).map(c -> new TeamPoints(c, team, 1));
    }


    protected static class TeamPoints {
        private final Cord cord;
        private final Team team;
        private final int points;

        protected TeamPoints(Cord cord, Team team, int points) {
            this.cord = cord;
            this.team = team;
            this.points = points;
        }
    }
}

@Deprecated
class ZoneOfControlAtCordCalculator extends ZoneOfControlCalculator {
    private final Cord cord;
    private final int maxDistance;


    ZoneOfControlAtCordCalculator(Cord cord, SectionDefinition sectionDefinition, Map<Cord, Unit> units, Optional<Predicate<Cord>> unitFilter, int maxDistance) {
        super(sectionDefinition, units, unitFilter);

        this.cord = cord;
        this.maxDistance = maxDistance;
    }

    protected Stream<TeamPoints> process(Map.Entry<Cord, Unit> unitAtCord) {
        var team = unitAtCord.getValue().team;
        var unitCord = unitAtCord.getKey();
        int points = 1 + maxDistance - super.sectionDefinition.distance(unitCord, cord);
        assert points >= 0;
        if (points < -0) points *= -1;
        return Stream.of(new TeamPoints(cord, team, points));
    }

    Optional<CellControl> cordControl() {
        var cc = result().findFirst();
        assert points.size() <= 1 : "Should only have collected point for cord:" + cord;
        return cc;
    }
}
