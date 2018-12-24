package extensions.java.util.Map;


import world.Player;
import world.Team;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;


public class MapExtension {


    /**
     * Split the point assigned to team between the players
     */
    public static Map<Player, Integer> splitPoints(Map<Team, Integer> points, Stream<Player> betweenPlayers) {
        var players = betweenPlayers.collect(toList());
        var teamToPlayer = players.stream().collect(groupingBy(p -> p.team, summingInt(i -> 1)));

        return players.stream().collect(toUnmodifiableMap(p -> p, p -> (int) Math.round(1.0 * points.getOrDefault(p.team, Integer.valueOf(0)) / teamToPlayer.get(p.team))));
    }
}
