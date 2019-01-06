package world;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;

public class Rect {

    @Nonnull
    public final int col;
    @Nonnull
    public final int row;
    @Nonnull
    public final int width;
    @Nonnull
    public final int height;


    public Rect(int col, int row, int width, int height) {
        this.col = col;
        this.row = row;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("col", col)
                .add("row", row)
                .add("width", width)
                .add("height", height)
                .toString();
    }
}
