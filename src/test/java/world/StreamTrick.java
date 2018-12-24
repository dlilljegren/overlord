package world;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.IntStream;

public class StreamTrick {

    @Test
    public void generatePairs() {

        IntStream.range(0, 3 * 10).mapToObj(i -> new Pair(i % 10, 12 + i % 3)).forEach(System.out::println);
    }


    private static class Pair {
        private final int x;
        private final int y;

        private Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return String.format("%d,%d", x, y);
        }
    }

    @Test
    public void testSetsDifference() {
        var bigger = Set.of(1, 2, 3, 4, 5, 6, 7);
        var smaller = Set.of(-1, 0, 1, 2, 3);

        System.out.println(Sets.difference(smaller, bigger));
    }
}
