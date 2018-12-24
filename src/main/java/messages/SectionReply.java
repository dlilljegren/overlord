package messages;

import changes.SectionUpdate;
import com.google.common.base.MoreObjects;
import world.Cord;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public abstract class SectionReply {

    public static class Snapshot extends SectionReply {

        @Nonnull
        public final SectionUpdate.Snapshot snapshot;

        public Snapshot(SectionUpdate.Snapshot snapshot) {
            this.snapshot = requireNonNull(snapshot);

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("snapshot", snapshot)
                    .toString();
        }
    }

    public static class AddUnitFailed extends SectionReply {
        @Nonnull
        public final String reason;
        @Nonnull
        public final Cord cord;


        public AddUnitFailed(@Nonnull String reason, @Nonnull Cord cord) {
            this.reason = reason;
            this.cord = cord;
        }
    }
}
