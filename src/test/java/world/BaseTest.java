package world;

import com.google.common.collect.Sets;
import extensions.java.util.Set.HexExtension;
import games.GameDefinition;
import games.GameDefinitions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseTest {


    @ParameterizedTest
    @MethodSource("games")

    public void TestBase(GameDefinition game, Set<Cord> area, Set<Cord> zoc) {
        var underTest = Base.createInSection(Cord.at(0, 0), 2, game.worldDefinition());

        assertEquals(area, underTest.area);
        assertEquals(zoc, underTest.zoneOfControl);

    }

    private static Stream<Arguments> games() {
        Set<Cord> areaSquare = Set.of(
                Cord.at(0, 0),
                Cord.at(0, 1),
                Cord.at(1, 0),
                Cord.at(0, -1),
                Cord.at(-1, 0)
        );

        Set<Cord> zocSquare = Set.of(
                Cord.at(1, 1),
                Cord.at(-1, -1),
                Cord.at(1, -1),
                Cord.at(-1, 1),
                Cord.at(2, 0),
                Cord.at(0, 2),
                Cord.at(-2, 0),
                Cord.at(0, -2));


        var origo = Hex.atQRS(0, 0, 0);
        Set<Hex> areaHex = Set.of(
                origo,
                origo.neighbor(0),
                origo.neighbor(2),
                origo.neighbor(4)
        );

        var hexSd = (SectionDefinition.Hex) GameDefinitions.SMALL_HEX.worldDefinition().sectionDefinition;
        var areaHexAsCord = HexExtension.toCord(areaHex, hexSd);

        var zocHex = Set.of(
                origo.neighbor(1), origo.neighbor(3), origo.neighbor(5)
        );
        zocHex = Sets.newHashSet(zocHex);
        zocHex.addAll(origo.circle(2));


        System.out.println(origo.circle(2));

        return Stream.of(
                Arguments.of(GameDefinitions.SMALL, areaSquare, zocSquare),
                Arguments.of(GameDefinitions.SMALL_HEX, areaHexAsCord, HexExtension.toCord(zocHex, hexSd)));
    }

}