package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class CellControl {
    public final Map<Team, Integer> teamPoints;
    public final Cord cord;

    CellControl(final Cord cord) {
        this(cord, Maps.newHashMap());
    }

    @CompiledJson
    CellControl(final Cord cord, Map<Team, Integer> teamPoints) {
        this.cord = cord;
        this.teamPoints = teamPoints;
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

    public int points(Team team) {
        var val = teamPoints.get(team);
        if (val == null) return 0;
        return val;
    }
}
