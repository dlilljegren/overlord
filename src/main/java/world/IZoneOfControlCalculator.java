package world;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the current zone of controls changes for a section
 */
public interface IZoneOfControlCalculator {


    ZocResultSnapshot calculateSnapshot(Map<Cord, Unit> units);

    ZocResultDelta calculateDelta(Map<Cord, Unit> units, Collection<Cord> removed);


    class ZocResultSnapshot {
        @Nonnull
        public final Map<Team, Set<Cord>> teamToCord;


        public ZocResultSnapshot(@Nonnull Map<Team, Set<Cord>> teamToCord) {
            this.teamToCord = teamToCord;
        }
    }

    class ZocResultDelta extends ZocResultSnapshot {
        @Nonnull
        public final Set<Cord> removed;

        public ZocResultDelta(@Nonnull Map<Team, Set<Cord>> teamToCord, @Nonnull Set<Cord> removed) {
            super(teamToCord);
            this.removed = removed;
        }
    }
}
