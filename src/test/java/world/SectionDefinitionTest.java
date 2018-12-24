package world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionDefinitionTest {

    @ParameterizedTest
    @MethodSource("sectionDefinitionProvider")
    void inSection(SectionDefinition underTest) {
        assertTrue(underTest.inSection(Cord.at(-10, -10)));
        assertTrue(underTest.inSection(Cord.at(10, 10)));
        assertFalse(underTest.inSection(Cord.at(-11, -10)));
        assertFalse(underTest.inSection(Cord.at(-10, -11)));
        assertFalse(underTest.inSection(Cord.at(11, 0)));
        assertFalse(underTest.inSection(Cord.at(0, 11)));

    }

    @Test
    void area() {
    }

    @Test
    void circleArea() {
    }


    private static Stream<SectionDefinition> sectionDefinitionProvider() {
        return Stream.of(new SectionDefinition.Square(21, 21), new SectionDefinition.Hex(21, 21, HexType.ODD_Q));
    }
}