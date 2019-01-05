package world;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ZocResult {
    @Nonnull
    public final Map<Team, Set<Cord>> gained;

    @Nonnull
    public final Map<Team, Set<Cord>> lost;
    private final boolean isSnapshot;

    private ZocResult(@Nonnull Map<Team, Set<Cord>> gained, @Nonnull Map<Team, Set<Cord>> lost, boolean isSnapshot) {
        this.gained = gained;
        this.lost = lost;
        this.isSnapshot = isSnapshot;
    }

    static ZocResult snapshot(@Nonnull Map<Team, Set<Cord>> gained) {
        return new ZocResult(gained, Collections.EMPTY_MAP, true);
    }

    static ZocResult delta(@Nonnull Map<Team, Set<Cord>> gained, @Nonnull Map<Team, Set<Cord>> lost) {
        return new ZocResult(gained, lost, false);
    }

    public boolean isSnapshot() {
        return false;
    }

    public boolean isEmpty() {
        return gained.isEmpty() && lost.isEmpty();
    }
}
