package world;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamManagerTest {

    @Test
    void recommendedTeam() {
        var teams = List.of(Teams.teamForName("red"), Teams.teamForName("blue"));

        var underTest = new TeamManager(teams);

        var red1 = Teams.teamForName("red").createPlayer(1);
        var red2 = Teams.teamForName("red").createPlayer(2);
        var blue1 = Teams.teamForName("blue").createPlayer(1);

        //Add 1 red and 2 blue Players

        underTest.activatePlayer(red1);
        underTest.activatePlayer(red2);
        underTest.activatePlayer(blue1);

        var team1 = underTest.recommendedTeam();

        assertEquals(Teams.teamForName("blue"), team1);

        //Remove the blue
        underTest.deactivatePlayer(blue1);
        var team2 = underTest.recommendedTeam();
        assertEquals(Teams.teamForName("blue"), team2);

    }

}