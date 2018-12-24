package world;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static extensions.java.util.Map.MapExtension.splitPoints;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

class SectorUnitAllocation {
    private final Map<Cord, Unit> units;
    private final Predicate<Cord> filter;
    private final int newUnits;

    SectorUnitAllocation(Map<Cord, Unit> units, Predicate<Cord> filter, int newUnits) {
        this.newUnits = newUnits;
        this.units = units;
        this.filter = filter;
    }


    Map<Team, Integer> partitionAmongTeams() {
        var r1 = units.entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .collect(groupingBy(e -> e.getValue().team, summingInt(t -> 1)));

        var totalUnits = r1.values().stream().mapToInt(i -> i).sum();


        AtomicInteger index = new AtomicInteger();
        r1.entrySet().forEach((e) -> {
            var val = e.getValue() * 1.0 * newUnits / totalUnits;
            int points = index.getAndAdd(1) % 2 == 0 ? (int) Math.floor(val) : (int) Math.ceil(val);
            e.setValue(points);
        });

        return ImmutableMap.copyOf(r1);
    }

    Map<Player, Integer> allocatePerPlayer(Stream<Player> players) {


        var teamToUnitsCount = partitionAmongTeams();
        return splitPoints(teamToUnitsCount, players);

        /*
        var teamToPlayer =
                players.collect(
                    toImmutableListMultimap(p->p.team, Function.identity()));
        */
        /*
        var teamPoints =units.entrySet().stream()
                .filter(e->filter.test(e.getKey()))
                .map(StateChangeBackedMap.Entry::getValue)
                .collect(groupingBy(Unit::team,counting()));
        */

        /*
        var playerToTeam = teamToPlayer.inverse();

        var totalUnits = units.size();

        Function<StateChangeBackedMap.Entry<Player,Team>,Integer> valueCalculator = e->{
            var team = e.getValue();
            var pointsForTeam = teamToUnitsCount.getOrDefault(team,0)*1.0f/totalUnits;
            var pointsPerPlayerInTeam = (pointsForTeam / teamToPlayer.get(team).size());
            return Math.round(pointsPerPlayerInTeam*newUnits);
        };

        return playerToTeam.entries().stream().collect(toUnmodifiableMap(e->e.getKey(),valueCalculator));
        */
    }


}
