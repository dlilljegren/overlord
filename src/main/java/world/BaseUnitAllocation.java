package world;


import extensions.java.util.Map.MapExtension;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Calculate units allocated per team due to ownership of the zone of control of the base
 */
public class BaseUnitAllocation {
    private final Map<Cord, Unit> units;
    private final Collection<Base<Cord>> bases;
    private final Integer unitsAllocatedPerBase;

    public BaseUnitAllocation(Map<Cord, Unit> units, Collection<Base<Cord>> bases, Integer unitsAllocatedPerBase) {
        this.units = units;
        this.bases = bases;
        this.unitsAllocatedPerBase = unitsAllocatedPerBase;
    }

    Map<Player, Integer> allocatePerPlayer(Stream<Player> players) {
        var teams = units.entrySet().stream().collect(toUnmodifiableMap(e -> e.getKey(), e -> e.getValue().team));

        var teamToPoints = bases.stream().map(b -> b.owner(teams)).filter(Optional::isPresent).collect(toUnmodifiableMap(o -> o.get(), o -> unitsAllocatedPerBase));

        return MapExtension.splitPoints(teamToPoints, players);
    }
}
