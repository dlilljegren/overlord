package world;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * Keep track of teams and their active players
 */
public class TeamManager {

    private final Map<Team, Collection<Player>> teamToActivePlayers;
    private final Set<Team> validTeams;

    public TeamManager(Collection<Team> teams) {
        if (teams.isEmpty()) throw new IllegalArgumentException("teams Cant be empty");
        teamToActivePlayers = Maps.newHashMap();
        validTeams = Sets.newHashSet(teams);
        validTeams.forEach(t -> teamToActivePlayers.put(t, Sets.newHashSet()));

    }

    public boolean activatePlayer(Player player) {
        assert player != null;
        assert validTeams.contains(player.team) : format("Player [%s] refer to Team [%s] but that team doesn't exist", player.name, player.team);

        var playersInTeam = teamToActivePlayers.get(player.team);
        var wasNotActive = playersInTeam.add(player);
        return wasNotActive;
    }

    public boolean deactivatePlayer(Player player) {
        assert player != null;
        assert validTeams.contains(player.team) : format("Player [%s] refer to Team [%s] but that team doesn't exist", player.name, player.team);

        var players = teamToActivePlayers.get(player.team);
        return players.remove(player);
    }

    public Team recommendedTeam() {
        var teamWithLeastActive = teamToActivePlayers.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().size())).findFirst();
        return teamWithLeastActive.map(e -> e.getKey()).get();
    }
}
