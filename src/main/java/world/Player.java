package world;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Player {
    public final String name;
    public final Team team;


    public Player(String name, Team team) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(team);
        this.name = name;
        this.team = team;

    }

    public Team team() {
        return this.team;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("team", team)
                .toString();
    }

    public Unit createUnit() {
        return new Unit(team);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) &&
                Objects.equals(team, player.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, team);
    }
}
