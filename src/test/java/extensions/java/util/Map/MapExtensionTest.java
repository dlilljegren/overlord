package extensions.java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import world.Team;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static world.Teams.teamForName;

class MapExtensionTest {

    @Test
    public void testSplitPoints() {
        var red = teamForName("red");
        var blue = teamForName("blue");
        var green = teamForName("green");


        var players = Stream.of(red.createPlayer(1), red.createPlayer(2), blue.createPlayer(1), green.createPlayer(1));

        var teamToPoints = new ImmutableMap.Builder<Team, Integer>().put(red, 5).put(blue, 3).build();

        var result = MapExtension.splitPoints(teamToPoints, players);

        assertEquals(Math.round(10 * 0.5f * 0.5f), (int) result.get(red.createPlayer(1)));
        assertEquals((int) (10 * 0.3), (int) result.get(blue.createPlayer(1)));
        assertEquals(0, (int) result.get(green.createPlayer(1)));
    }

}