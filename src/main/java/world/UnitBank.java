package world;

public class UnitBank {

    private int availableUnits = 0;

    public void addUnits(int u) {
        this.availableUnits += u;
    }

    public boolean hasUnits() {
        return this.availableUnits > 1;
    }

    public int balance() {
        return this.availableUnits;
    }
}
