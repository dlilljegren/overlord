package world;

import javax.annotation.Nonnull;

public class Unit {

    @Nonnull
    public final Team team;


    public Unit(Team team) {
        this.team = team;
    }

    public static Unit forTeam(String teamColor) {
        return new Unit(Teams.teamForName(teamColor));
    }

    public static Unit forTeam(Team team) {
        return new Unit(team);//ToDo should maybe have a singleton cache
    }

    public Team team() {
        return this.team;
    }

    public int defense() {
        return 1;
    }

    public int attack() {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("Unit-%s", team.name());
    }
}
