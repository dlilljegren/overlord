package world;


import games.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//see https://github.com/Pragmatists/JUnitParams

class WorldDefinitionTest {

    private final WorldDefinition underTest = new WorldDefinition(3, 3, 1, 1, 27, new Rules() {
    }, SectionDefinition.create(5, 5, SectionDefinition.GridType.SQUARE));

    private final WorldDefinition underTest_3x3_5x5_2_1 = new WorldDefinition(3, 3, 2, 1, 27, new Rules() {
    }, SectionDefinition.create(5, 5, SectionDefinition.GridType.SQUARE));

    private final WorldDefinition underTest_2x2_3x3_1_1 = new WorldDefinition(2, 2, 1, 1, 27, new Rules() {
    }, SectionDefinition.create(3, 3, SectionDefinition.GridType.SQUARE));

    @ParameterizedTest
    @CsvSource({"0, ", "1,  ", "2,  ",
            "3, ", "4, 0", "5, 1",
            "6, ", "7, 3", "8, 4"
    })
    void northWestSection(int section, Integer expected) {
        assertEquals(expected, underTest.northWestSection(section).orElse(null));
    }

    @ParameterizedTest
    @CsvSource({"0, ", "1,  ", "2,  ",
            "3,0", "4, 1", "5, 2",
            "6,3", "7, 4", "8, 5"
    })
    void northSection(int section, Integer expected) {
        assertEquals(expected, underTest.northSection(section).orElse(null));
    }

    @ParameterizedTest
    @CsvSource({"0, ", "1, 0", "2, 1",
            "3, ", "4, 3", "5, 4",
            "6, ", "7, 6", "8, 7"
    })
    void westSection(int section, Integer expected) {
        assertEquals(expected, underTest.westSection(section).orElse(null));
    }


    @ParameterizedTest
    @CsvSource({"0, 0, 0", "0, 1, 0", "0, 2, 0", "1, 0, 0", "1, 1, 0", "1, 2, 0", "2, 0, 0", "2, 1, 0", "2, 2, 0",
            "3, 0, 1", "3, 1, 1", "3, 2, 1", "4, 0, 1", "4, 1, 1", "4, 2, 1",
            "0 ,3, 2", "0,4,2", "1,3,2", "1,4,2", "2,3,2", "2,4,2",
            "3 ,3, 3", "4,4,3", "3,4,3", "4,3,3"
    })
    void masterSection(int x, int y, int expected) {
        assertEquals(expected, underTest_2x2_3x3_1_1.world.masterSectionAt(WorldCord.at(x, y)));
    }

    @ParameterizedTest
    @CsvSource({"0, 0, 0", "4, 0, 0", "0,4,0", "4,0,0",
            "10, 12, 8", "8,12,8", "10,9,8", "8,9,8"
    })
    void masterSection_3x3_5x5_2_1(int x, int y, int expected) {
        assertEquals(expected, underTest_3x3_5x5_2_1.world.masterSectionAt(WorldCord.at(x, y)));
    }

    @ParameterizedTest
    @CsvSource({
            "0, -1, -1, 0, 0",
            "0,  0,  0, 1, 1",

            "1, -1, -1, 2, 0",
            "1,  1,  1, 4, 2",

            "2, -1, -1, 0, 2",
            "2,  1,  1, 2, 4",

    })
    void fromSectionToWorld(int sectionNo, int sc, int sr, int expectedWc, int expectedWr) {
        var expected = WorldCord.at(expectedWc, expectedWr);
        var sectionCord = Cord.at(sc, sr);

        assertEquals(expected, underTest_2x2_3x3_1_1.section.toWorld(sectionNo).apply(sectionCord));

        //The reverse world to mainSection
        assertEquals(sectionCord, underTest_2x2_3x3_1_1.world.toSection(sectionNo).apply(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "3,  6,  0, 2",
            "3,  4,  1, 0",
            "3,  4,  2, 0",

            "3,  7,  1, 2",
            "3,  7,  2, 2",

    })
    void fromSectionToSection(int fromSection, int toSection, int col, int row) {
        var underTest = underTest_3x3_5x5_2_1;//2 == -2
        var start = Cord.at(col, row);

        System.out.println("In World:" + underTest.section.toWorld(3).apply(start));
        System.out.println("Back World:" + underTest.world.toSection(3).apply(underTest.section.toWorld(3).apply(start)));

        var fromTo = underTest.section.fromTo(fromSection, toSection);
        var cordInTo = fromTo.apply(start);
        assertTrue(underTest.sectionDefinition.inSection(cordInTo), "Bad " + cordInTo);

        //Now reverse
        var toFrom = underTest.section.fromTo(toSection, fromSection);
        var cordInFrom = toFrom.apply(cordInTo);
        assertTrue(underTest.sectionDefinition.inSection(cordInFrom));

        assertEquals(start, cordInFrom);

    }


    @Test
    void worldSize() {
        assertEquals(5, underTest_2x2_3x3_1_1.worldWidth);
        assertEquals(5, underTest_2x2_3x3_1_1.worldHeight);

        assertEquals(3 * 5 - 2, underTest.worldTotalColumns());
        assertEquals(3 * 5 - 2, underTest.worldTotalRows());

        assertEquals(3 * 5 - 2 * 2, underTest_3x3_5x5_2_1.worldTotalColumns());
        assertEquals(3 * 5 - 2, underTest_3x3_5x5_2_1.worldTotalRows());
    }
}