package world;

import changes.SectionUpdateBuilder;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Combat {

    private final SectionUpdateBuilder changes;

    private final Set<Cord> masterUnits;

    private final Map<Cord, Unit> units;
    private final Predicate<Cord> isMaster;

    private final Function<Cord, Stream<Cord>> neighbours;

    Combat(Map<Cord, Unit> units, Predicate<Cord> isMaster, Function<Cord, Stream<Cord>> neighbours) {
        this.units = units;
        this.isMaster = isMaster;
        this.neighbours = neighbours;
        this.changes = new SectionUpdateBuilder();

        this.masterUnits = units.keySet().stream().filter(isMaster).collect(Collectors.toSet());
    }

    SectorCombatResult combat(SectionVersion next) {
        //Find distinct teams
        units.values().stream()
                .map(Unit::team)
                .distinct()
                .forEach(this::combatTeam);

        SectorCombatStats combatStats = new SectorCombatStats();
        return new SectorCombatResult(changes.buildDelta(next), combatStats);
    }

    void combatTeam(Team team) {


        Map<Cord, Integer> defensePoints = calc(thisTeam(team), this::defense);
        Map<Cord, Integer> enemyPoints = calc(otherTeams(team), this::attack);

        Map<Cord, Integer> points = Stream.of(defensePoints, enemyPoints)
                .flatMap(m -> m.entrySet().stream())
                .filter(relevant(team))//Only care about the cords were there are actual units of this team and are own by the sectionNo
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (d, a) -> a + d));

        points.entrySet()
                .stream()
                .filter(e -> e.getValue().intValue() < 0)
                .map(Map.Entry::getKey)
                .forEach(changes::removeUnit);

    }

    Map<Cord, Integer> calc(Predicate<Map.Entry<Cord, Unit>> filter, ToIntFunction<Map.Entry<Cord, Unit>> calculator) {
        return units.entrySet().parallelStream()
                .filter(filter)
                .flatMap(this::neighbours)
                .collect((Collectors.groupingByConcurrent(e -> e.getKey(),
                        Collectors.summingInt(calculator))));
    }

    private Stream<Map.Entry<Cord, Unit>> neighbours(Map.Entry<Cord, Unit> entry) {
        Unit unit = entry.getValue();
        return Stream.concat(
                Stream.of(entry),
                neighbours.apply(entry.getKey())
                        .map(cord -> new AbstractMap.SimpleImmutableEntry<>(cord, unit)));
    }


    int defense(Map.Entry<Cord, Unit> entry) {
        return entry.getValue().defense();
    }

    int attack(Map.Entry<Cord, Unit> entry) {
        return -entry.getValue().attack();
    }

    Predicate<Map.Entry<Cord, Unit>> thisTeam(Team team) {
        return e -> e.getValue().team.equals(team);
    }

    Predicate<Map.Entry<Cord, Unit>> otherTeams(Team team) {
        return thisTeam(team).negate();
    }

    Predicate<Map.Entry<Cord, Integer>> relevant(Team team) {
        return e -> {
            Unit u = units.get(e.getKey());
            return u != null && u.team.equals(team) && isMaster.test(e.getKey());
        };
    }
}
