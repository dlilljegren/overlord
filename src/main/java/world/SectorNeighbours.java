package world;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static world.SectorNeighbours.Relation.*;


public class SectorNeighbours {


    enum Relation {Master, Slave, None}

    private final Set<Integer> slaves;
    private final Set<Integer> masters;
    private final Set<Integer> nones;

    public final Set<Integer> all;

    public SectorNeighbours(Set<Integer> slaves, Set<Integer> masters, Set<Integer> nones) {
        this.slaves = slaves;
        this.masters = masters;
        this.nones = nones;

        this.all = Sets.union(Sets.union(masters, slaves), nones).immutableCopy();
    }


    public static SectorNeighbours create(WorldDefinition world, int sectionNoCenter) {
        SectionNeighbour me = new SectionNeighbour(world, sectionNoCenter);

        Map<Relation, Set<Integer>> masterAndSlaves = me.neighbours().collect(
                Collectors.groupingBy(
                        c -> c.relation(me),
                        Collectors.mapping(c -> c.center, Collectors.toSet())
                )
        );

        return new SectorNeighbours(
                masterAndSlaves.getOrDefault(Slave, Collections.EMPTY_SET),
                masterAndSlaves.getOrDefault(Master, Collections.EMPTY_SET),
                masterAndSlaves.getOrDefault(None, Collections.EMPTY_SET)
        );
    }

    public Set<Integer> slaves() {
        return slaves;
    }

    public Set<Integer> masters() {
        return masters;
    }

    public Set<Integer> normals() {
        return nones;
    }

    public Set<Integer> all() {
        return all;
    }

    public boolean isNeighbour(int neighbourSector) {

        return all.contains(neighbourSector);
    }

    private static class SectionNeighbour implements Comparable<SectionNeighbour> {


        private final WorldDefinition worldDefinition;
        private final int center;

        private boolean valid;

        private SectionNeighbour(WorldDefinition worldDefinition, int center) {
            this.worldDefinition = worldDefinition;
            this.center = center;

            this.valid = center >= 0 && center < worldDefinition.totalNoOfSectors();
        }

        Stream<SectionNeighbour> neighbours() {
            return Stream.of(east(), southEast(), south(), southWest(), west(), northWest(), north(), northEast()).filter(sectionNeighbour -> sectionNeighbour.valid);
        }

        Relation relation(SectionNeighbour other) {
            if (isSlaveOf(other)) return Slave;
            if (isMasterOf(other)) return Master;
            return None;
        }


        /**
         * Slaves are to the east or south
         *
         * @param other
         * @return true if this is slave to other
         */
        boolean isSlaveOf(SectionNeighbour other) {
            if (!valid) return false;
            return other.east().center == this.center ||
                    other.southEast().center == this.center ||
                    other.south().center == this.center;
        }

        /**
         * Masters are to the west and north
         *
         * @param other
         * @return true if this is Master of other
         */
        boolean isMasterOf(SectionNeighbour other) {
            if (!valid) return false;
            return other.isSlaveOf(this);
        }

        private SectionNeighbour south() {
            return new SectionNeighbour(worldDefinition, center + worldDefinition.gridWidth);
        }

        private SectionNeighbour north() {
            return new SectionNeighbour(worldDefinition, center - worldDefinition.gridWidth);
        }

        private SectionNeighbour east() {
            SectionNeighbour c = new SectionNeighbour(worldDefinition, center + 1);
            c.valid = c.valid && c.row() == this.row();
            return c;
        }

        private SectionNeighbour west() {
            SectionNeighbour c = new SectionNeighbour(worldDefinition, center - 1);
            c.valid = c.valid && c.row() == this.row();
            return c;
        }

        private SectionNeighbour northEast() {
            return north().east();
        }

        private SectionNeighbour northWest() {
            return north().west();
        }

        private SectionNeighbour southEast() {
            return south().east();
        }

        private SectionNeighbour southWest() {
            return south().west();
        }

        private int row() {
            return this.center / worldDefinition.gridWidth;
        }

        @Override
        public int compareTo(SectionNeighbour o) {
            return Integer.compare(center, o.center);
        }
    }
}
