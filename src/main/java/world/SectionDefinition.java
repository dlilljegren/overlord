package world;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;

public abstract class SectionDefinition {
    private static final Random R = new Random();
    public final int cols;
    public final int rows;

    public final Predicate<Cord> inSection;

    protected SectionDefinition(int cols, int rows) {
        if (cols % 2 == 0) throw new IllegalArgumentException(format("Cols not odd %d", cols));
        if (rows % 2 == 0) throw new IllegalArgumentException(format("Rows not odd %d", rows));
        this.cols = cols;
        this.rows = rows;

        this.inSection = sc -> isValidCol(sc.col) && isValidRow(sc.row);
    }

    public static SectionDefinition create(int cols, int rows, GridType gridType) {
        return gridType.create(cols, rows);
    }


    public Stream<Cord> all() {
        IntFunction<Cord> cordCreator = i -> {
            int col = i % cols - cols / 2;
            int row = i / cols - rows / 2;
            return Cord.at(col, row);
        };

        return IntStream.range(0, rows * cols).mapToObj(cordCreator);
    }


    public abstract Stream<Cord> neighbours(Cord cord);

    public abstract int distance(Cord a, Cord b);

    private boolean isValidCol(int col) {
        return Math.abs(col) <= cols / 2;
    }

    private boolean isValidRow(int row) {
        return Math.abs(row) <= rows / 2;
    }

    public boolean inSection(Cord cord) {
        return inSection.test(cord);
    }

    public int area() {
        return cols * rows;
    }

    public Cord randomCord() {
        return Cord.at(-cols + R.nextInt(cols), -rows + R.nextInt(rows));
    }

    public abstract Predicate<Cord> circleArea(Cord cord, int radius);

    public abstract Predicate<Cord> cross(Cord center);

    public abstract GridType gridType();


    public Stream<Cord> fillArea(Predicate<Cord> geometry) {
        return all().filter(geometry).filter(this::inSection);
    }

    public Stream<Cord> cordsInCircle(Cord cord, int radius) {
        //ToDo this is slow
        return fillArea(circleArea(cord, radius));
    }


    public enum GridType {
        SQUARE, HEX;

        SectionDefinition create(int cols, int rows) {
            switch (this) {
                case SQUARE:
                    return new SectionDefinition.Square(cols, rows);
                case HEX:
                    return new SectionDefinition.Hex(cols, rows, HexType.ODD_Q);
                default:
                    throw new RuntimeException("Unknown gridType:" + this);
            }
        }
    }

    static class Square extends SectionDefinition {

        Square(int cols, int rows) {
            super(cols, rows);
        }

        @Override
        public Stream<Cord> neighbours(Cord cord) {
            return Arrays.stream(Cord.directions).map(cord::add).filter(this::inSection);
        }

        @Override
        public int distance(Cord a, Cord b) {
            return a.manhattanDistance(b);

        }

        @Override
        public Predicate<Cord> circleArea(Cord cord, int radius) {
            return other -> other.manhattanDistance(cord) <= radius;
        }

        @Override
        public Predicate<Cord> cross(Cord center) {
            return other -> other.manhattanDistance(center) <= 1;
        }

        @Override
        public GridType gridType() {
            return GridType.SQUARE;
        }


    }

    public static class Hex extends SectionDefinition {
        private final HexType hexType;

        Hex(int cols, int rows, HexType hexType) {
            super(cols, rows);
            this.hexType = hexType;
        }

        @Override
        public Stream<Cord> neighbours(Cord cord) {
            world.Hex hex = hexType.toHex(cord);
            return Arrays.stream(world.Hex.directions).map(hex::add).map(hexType::toCord).filter(this::inSection);
        }

        @Override
        public int distance(Cord a, Cord b) {
            var ha = hexType.toHex(a);
            var hb = hexType.toHex(b);
            return ha.distance(hb);
        }

        @Override
        public Predicate<Cord> circleArea(Cord cord, int radius) {
            var hex = hexType.toHex(cord);
            return other -> hexType.toHex(other).distance(hex) <= radius;
        }


        @Override
        public Predicate<Cord> cross(Cord center) {
            var inCross = IntStream.of(0, 2, 4).mapToObj(i -> world.Hex.direction(i)).map(hexType::toCord).collect(Collectors.toSet());
            inCross.add(center);
            return other -> inCross.contains(other);
        }

        @Override
        public GridType gridType() {
            return GridType.HEX;
        }

        public Cord toCord(world.Hex hex) {
            return hexType.toCord(hex);
        }
    }

}

