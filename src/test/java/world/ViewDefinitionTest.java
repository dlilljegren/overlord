package world;

import games.GameDefinitions;
import games.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewDefinitionTest {

    private final WorldDefinition wd_3x3_5x5_2_1 = new WorldDefinition(3, 3, 2, 1, 27, new Rules() {
    }, SectionDefinition.create(5, 5, SectionDefinition.GridType.SQUARE));


    @ParameterizedTest
    @CsvSource({"0, 0, 0",
            "1, 3, 0",
            "2, 6, 0",
            "3, 0, 4",
    })
    void sectionToViewOrigo(int sectionNo, int wcCol, int wcRow) {
        var underTest = ViewDefinition.create(wd_3x3_5x5_2_1, sectionNo, 5, 5);


        //The origo of the view, should be in the middle of the mainSection
        assertEquals(WorldCord.at(wcCol, wcRow), underTest.origo);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0, 0, 0",
            "0, 4, 4, 4, 4",
            "0, 10, 10, 10, 10",

            "1, 0, 0, 3, 0",
            "8, 0, 0, 6, 8"
    })
    void viewToWorld(int section, int vCol, int vRow, int expectedWorldCol, int expectedWorldRow) {
        var underTest = View.create(wd_3x3_5x5_2_1, section, 5, 5);


        assertEquals(WorldCord.at(expectedWorldCol, expectedWorldRow), underTest.viewToWorld(WorldCord.at(vCol, vRow)));

        //And back
        assertEquals(WorldCord.at(vCol, vRow), underTest.worldToView(WorldCord.at(expectedWorldCol, expectedWorldRow)));
    }

    @Test
    void addUnit_conversion_test() {
        var viewCord = WorldCord.at(2, 2);

        var underTest = View.create(wd_3x3_5x5_2_1, 0, 5, 5);

        var section = underTest.masterSection(viewCord);
        assertEquals(0, section);

        var sectionCord = underTest.viewToSection(section).apply(viewCord);

        assertEquals(Cord.at(0, 0), sectionCord);

        assertEquals(viewCord, underTest.sectionToView(section).apply(Cord.at(0, 0)));

    }

    @Test
    void sectionsInView() {
        var worldWidth = wd_3x3_5x5_2_1.worldWidth;
        var worldHeight = wd_3x3_5x5_2_1.worldHeight;
        var underTest = View.create(wd_3x3_5x5_2_1, 0, worldWidth, worldHeight);

        var allSections = IntStream.range(0, 9).mapToObj(i -> Integer.valueOf(i)).collect(toUnmodifiableSet());

        assertEquals(allSections, underTest.masterSectionsInView());

        var wd = GameDefinitions.SMALL.worldDefinition();
        underTest = View.create(wd, 0, 40, 21);
        var expected = Set.of(0, 1, 2);
        assertEquals(expected, underTest.masterSectionsInView());
    }
}