package world;

public class Cell {


    public final Cord cord;
    public final int master;


    public Cell(Cord cord, int master) {
        this.cord = cord;
        this.master = master;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "cord=" + cord +
                ", master=" + master +
                '}';
    }
}


