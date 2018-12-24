package world;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SectionCordTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0",
            "1, 0, 1",
            "-2, 1, 3",
            "2, 1,3  ",
            "2, -1,3",
            "-5, -4,9",
    })
    void manhattanDistance(int col, int row, int expectedDistance) {
        var origo = Cord.at(0, 0);
        var cord = Cord.at(col, row);
        assertEquals(expectedDistance, origo.manhattanDistance(cord));
        assertEquals(expectedDistance, cord.manhattanDistance(origo));

    }
}