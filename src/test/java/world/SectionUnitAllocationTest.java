package world;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static world.Cord.at;
import static world.Teams.teamForName;
import static world.Unit.forTeam;

class SectionUnitAllocationTest {

    @Test
    public void test() {
        var red = teamForName("red");
        var blue = teamForName("blue");
        var green = teamForName("green");

        var units =
                new ImmutableMap.Builder<Cord, Unit>()
                        .put(at(0, 0), forTeam("red"))
                        .put(at(0, 1), forTeam("red"))
                        .put(at(0, 2), forTeam("red"))
                        .put(at(0, 3), forTeam("red"))
                        .put(at(1, 0), forTeam("blue"))
                        .put(at(1, 1), forTeam("blue"))
                        .put(at(1, 2), forTeam("blue"))
                        .put(at(1, 3), forTeam("blue"))
                        .build();


        var players = Stream.of(red.createPlayer(1), red.createPlayer(2), blue.createPlayer(1), green.createPlayer(1));

        var underTest = new SectorUnitAllocation(units, Predicates.alwaysTrue(), 10);

        var result = underTest.allocatePerPlayer(players);

        assertEquals(3, (int) result.get(red.createPlayer(1)));
        assertEquals((int) (10 * 0.5), (int) result.get(blue.createPlayer(1)));
        assertEquals(0, (int) result.get(green.createPlayer(1)));
    }
}