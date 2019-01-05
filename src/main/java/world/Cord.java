package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Local coordinate within a sectionNo
 */
public final class Cord implements Comparable<Cord>, ICord {
    public final int col;
    public final int row;


    private static final Map<Integer, Map<Integer, Cord>> cache = Maps.newConcurrentMap();

    public static final Cord ZERO = Cord.at(0, 0);

    static final Cord[] directions = new Cord[]{
            Cord.at(1, 0),
            Cord.at(1, 1),
            Cord.at(0, 1),
            Cord.at(-1, 1),
            Cord.at(-1, 0),
            Cord.at(-1, -1),
            Cord.at(0, -1),
            Cord.at(1, -1)
    };


    private Cord(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public static Stream<Cord> from(int[][] cr) {
        return Arrays.stream(cr).map(a -> Cord.at(a[0], a[1]));
    }

    public int col() {
        return this.col;
    }

    public int row() {
        return this.row;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
        /*
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cord that = (Cord) o;
        return col == that.col &&
                row == that.row;
                */
    }


    public int hashCode() {

        return Objects.hash(col, row);
    }

    @Override
    public String toString() {
        return String.format("%d:%d", col, row);
    }

    @CompiledJson
    public static Cord at(int col, int row) {
        return cache.computeIfAbsent(col, i -> Maps.newConcurrentMap()).computeIfAbsent(row, i -> new Cord(col, row));
    }


    public Cord add(Cord cord) {
        return at(col + cord.col, row + cord.row);
    }

    public Cord add(int deltaCol, int deltaRow) {
        return at(col + deltaCol, row + deltaRow);
    }

    @Override
    public int compareTo(Cord o) {
        int col = Integer.compare(this.col, o.col);
        if (col != 0) return col;
        return Integer.compare(this.row, o.row);
    }

    public int manhattanDistance(Cord b) {
        return Math.abs(col - b.col) + Math.abs(row - b.row);
    }
}
