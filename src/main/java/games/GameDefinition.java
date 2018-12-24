package games;

import map.IBaseGenerator;
import map.MapGenerator;
import world.Section;
import world.Team;
import world.WorldDefinition;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface GameDefinition {

    WorldDefinition worldDefinition();


    Stream<Team> availableTeams();

    /**
     * How often a sector starts combat
     *
     * @return
     */
    Duration combatCycle();

    /**
     * How often the sections should send out zone of control message
     *
     * @return
     */
    Duration zoneOfControlCycle();

    Duration standardTimeout();

    IJson serializer();

    Duration cursorBroadcastCycle();

    IBaseGenerator baseGenerator();

    default Stream<Section> createSections() {
        final var wc = worldDefinition();
        var mapSeed = wc.mapSeed;
        var gridWidth = wc.gridWidth;
        var gridHeight = wc.gridHeight;
        var mapGenerator = new MapGenerator(mapSeed, worldDefinition());
        var sectionToMap = mapGenerator.generateSectionMaps();
        var baseGenerator = Objects.requireNonNull(baseGenerator());
        return IntStream.range(0, gridWidth * gridHeight).mapToObj(i -> new Section(i, wc, sectionToMap.get(i), baseGenerator));
    }
}
