package world;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * From https://www.redblobgames.com/grids/hexagons/implementation.html
 */
public class Hex {
    static Hex[] directions = new Hex[]
            {
                    Hex.atQRS(+1, -1, 0),
                    Hex.atQRS(+1, 0, -1),
                    Hex.atQRS(0, +1, -1),
                    Hex.atQRS(-1, +1, 0),
                    Hex.atQRS(-1, 0, +1),
                    Hex.atQRS(0, -1, +1)
            };

    static public Hex[] diagonals = new Hex[]{
            Hex.atQRS(2, -1, -1),
            Hex.atQRS(1, -2, 1),
            Hex.atQRS(-1, -1, 2),
            Hex.atQRS(-2, 1, 1),
            Hex.atQRS(-1, 2, -1),
            Hex.atQRS(1, 1, -2)
    };


    public Hex(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;

        assert q + r + s == 0 : String.format("q:%d r:%d, s:%d", q, r, s);
    }

    final int q;
    final int r;
    final int s;

    public static Hex atQRS(int q, int r, int s) {
        return new Hex(q, r, s);
    }

    public Hex add(Hex b) {
        return new Hex(q + b.q, r + b.r, s + b.s);
    }


    public Hex subtract(Hex b) {
        return new Hex(q - b.q, r - b.r, s - b.s);
    }


    public Hex scale(int k) {
        return new Hex(q * k, r * k, s * k);
    }


    public Hex rotateLeft() {
        return new Hex(-s, -q, -r);
    }


    public Hex rotateRight() {
        return new Hex(-r, -s, -q);
    }

    public Hex neighbour(int n) {
        return add(directions[n]);
    }


    static public Hex direction(int direction) {
        return Hex.directions[direction];
    }


    public Hex neighbor(int direction) {
        return add(Hex.direction(direction));
    }

    public Collection<Hex> circle(int radius) {
        assert radius > 0;
        //this code doesn't work for radius == 0; can you see why?
        var cube = add(directions[4].scale(radius));

        List<Hex> results = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                results.add(cube);
                cube = cube.neighbor(i);
            }
        }
        return results;
    }

    public Hex diagonalNeighbor(int direction) {
        return add(Hex.diagonals[direction]);
    }


    public int length() {
        return ((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2);
    }


    public int distance(Hex b) {
        return subtract(b).length();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("q", q)
                .add("r", r)
                .add("s", s)
                .toString();
    }
}
