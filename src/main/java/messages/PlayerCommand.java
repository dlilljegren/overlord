package messages;

import com.google.common.base.MoreObjects;
import world.Cord;

import javax.annotation.Nonnull;

public abstract class PlayerCommand {

    private static abstract class CordAware extends PlayerCommand {
        @Nonnull
        public final Cord cord;
        @Nonnull
        public final int section;


        protected CordAware(int section, Cord cord) {
            this.cord = cord;
            this.section = section;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .add("mainSection", section)
                    .toString();
        }
    }

    public static class AddUnit extends CordAware {
        public AddUnit(int section, Cord cord) {
            super(section, cord);
        }


        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .add("section", section)
                    .toString();
        }

        public static class Failed extends CordAware {
            @Nonnull
            public final String reason;

            public Failed(String reason, int section, Cord cord) {
                super(section, cord);
                this.reason = reason;
            }
        }
    }

    public static class RemoveUnit extends CordAware {
        public RemoveUnit(int section, Cord cord) {
            super(section, cord);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("cord", cord)
                    .add("section", section)
                    .toString();
        }

        public static class Failed extends CordAware {
            @Nonnull
            public final String reason;

            public Failed(String reason, int section, Cord cord) {
                super(section, cord);
                this.reason = reason;
            }
        }
    }

    public static class AssignSection extends PlayerCommand {
        public final int sectionNo;

        public AssignSection(int sectionNo) {
            this.sectionNo = sectionNo;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("sectionNo", sectionNo)
                    .toString();
        }
    }

}
