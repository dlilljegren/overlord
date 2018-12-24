package world;

import com.dslplatform.json.CompiledJson;

import java.util.Objects;

/**
 * Cord in world coordinate system
 */
@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public class WorldCord implements ICord {
    public final int col;
    public final int row;


    private WorldCord(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int col() {
        return this.col;
    }

    public int row() {
        return this.row;
    }

    @CompiledJson
    public static WorldCord at(int col, int row) {
        return new WorldCord(col, row);
    }


    @Override
    public String toString() {
        return String.format("%d:%d", col, row);
    }

    public WorldCord add(int width, int height) {
        return WorldCord.at(col + width, row + height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldCord worldCord = (WorldCord) o;
        return col == worldCord.col &&
                row == worldCord.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }
}
