package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class CellControl {
    @Nonnull
    public final Map<Team, Integer> teamPoints;
    @Nonnull
    public final Cord cord;

    CellControl(final Cord cord) {
        this(cord, Maps.newHashMap());
    }

    @CompiledJson
    CellControl(final Cord cord, Map<Team, Integer> teamPoints) {
        this.cord = requireNonNull(cord);
        this.teamPoints = requireNonNull(teamPoints);
    }

    void add(Team team, int points) {
        teamPoints.compute(team, (t, prev) -> prev == null ? points : prev + points);
    }

    public Optional<Team> bestTeam() {
        return teamPoints.entrySet().stream().sorted(Comparator.comparingInt(z -> z.getValue() * -1)).map(Map.Entry::getKey).findFirst();
    }

    /**
     * How much this team has of the whole
     *
     * @param team
     * @return
     */
    public double teamPercentage(Team team) {
        return points(team) * 1.0 / teamPoints.values().stream().mapToInt(i -> i).sum();
    }

    public Cord cord() {
        return this.cord;
    }

    public int points(Team team) {
        var val = teamPoints.get(team);
        if (val == null) return 0;
        return val;
    }
}
