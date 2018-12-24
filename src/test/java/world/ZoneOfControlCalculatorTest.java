package world;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneOfControlCalculatorTest {


    public Optional<CellControl> test(SectionDefinition.GridType type, Map<Cord, Unit> units, int radius) {
        var cord = Cord.at(0, 0);
        var sd = SectionDefinition.create(5, 5, type);
        var circleArea = sd.circleArea(cord, radius);
        var underTest = new ZoneOfControlAtCordCalculator(cord, sd, units, Optional.of(circleArea), radius);

        return underTest.cordControl();
    }

    private Map<Cord, Unit> unitsInCircle() {
        var unit = new Unit(Teams.teamForName("red"));

        var cr = new int[][]{
                {1, 0},
                {1, 1},
                {0, 1},
                {-1, 1},
                {-1, 0},
                {-1, -1},
                {0, -1},
                {1, -1}
        };

        return Cord.from(cr).collect(Collectors.toMap(sc -> sc, sc -> unit));
    }

    @Test
    public void testSquare() {
        var valRadius0 = test(SectionDefinition.GridType.SQUARE, unitsInCircle(), 0);
        assertTrue(!valRadius0.isPresent());

        var valRadius1 = test(SectionDefinition.GridType.SQUARE, unitsInCircle(), 1);
        assertEquals(1.00, valRadius1.get().teamPercentage(Teams.teamForName("red")));
        assertEquals(4, valRadius1.get().points(Teams.teamForName("red")));// with a radius of one the west, east,north and south neighbour each count 1 point

        var valRadius2 = test(SectionDefinition.GridType.SQUARE, unitsInCircle(), 2);
        assertEquals(4 * 2 + 4, valRadius2.get().points(Teams.teamForName("red")));// with a radius of one the west, east,north and south neighbour each count 2 point, and we have 4 more contributing 1 point

        var valRadius3 = test(SectionDefinition.GridType.SQUARE, unitsInCircle(), 3);
        assertEquals(4 * 3 + 4 * 2, valRadius3.get().points(Teams.teamForName("red")));// with a radius of one the west, east,north and south neighbour each count 2 point, and we have 4 more contributing 1 point


    }

    @Test
    public void testHex() {
        var gridType = SectionDefinition.GridType.HEX;
        var valRadius0 = test(gridType, unitsInCircle(), 0);
        assertTrue(!valRadius0.isPresent());

        var valRadius1 = test(gridType, unitsInCircle(), 1);
        assertEquals(1.00, valRadius1.get().teamPercentage(Teams.teamForName("red")));
        assertEquals(6, valRadius1.get().points(Teams.teamForName("red")));// with a radius of 1, only the 6 neighbours

        var valRadius2 = test(gridType, unitsInCircle(), 2);
        assertEquals(6 * 2 + 2 * 1, valRadius2.get().points(Teams.teamForName("red")));// with a radius of 2 the 6 neighbours contributes 2 each and the other 2 1

        var valRadius3 = test(gridType, unitsInCircle(), 3);
        assertEquals(6 * 3 + 2 * 2, valRadius3.get().points(Teams.teamForName("red")));// with a radius of 3 the 6 neighbours contributes 3 and the other two 2


    }

}