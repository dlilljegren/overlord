package world;


import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SectorCellCalculator {


    private final int sectionSizeY;
    private final int sectionSizeX;

    private final int sectionNo;
    private final int overlapY;
    private final int overlapX;

    private final int northWest;
    private final int west;
    private final int north;

    private final Function<Cord, Cord> toMidSystem;

    SectorCellCalculator(int sectionNo, WorldDefinition worldDefinition) {
        this.sectionNo = sectionNo;
        this.sectionSizeY = worldDefinition.sectionDefinition.rows;
        this.sectionSizeX = worldDefinition.sectionDefinition.cols;
        this.overlapY = worldDefinition.rowSectionOverlap;
        this.overlapX = worldDefinition.colSectionOverlap;


        this.west = worldDefinition.westSection(this.sectionNo).orElse(sectionNo);
        this.north = worldDefinition.northSection(this.sectionNo).orElse(sectionNo);

        this.northWest = worldDefinition.northWestSection(this.sectionNo).orElse(this.west);

        this.toMidSystem = worldDefinition.section.toMid();
    }


    Stream<Cell> cells() {

        return IntStream.range(0, sectionSizeX * sectionSizeY).mapToObj(this::createCell);
    }

    private Cell createCell(int i) {
        int x = i % sectionSizeX;
        int y = i / sectionSizeX;

        int master = sectionNo;
        if (x < overlapX) {
            if (y < overlapY) {
                //NW
                master = northWest;
            } else {
                //left
                master = west;
            }
        } else if (y < overlapY) {
            //Top
            master = north;
        }
        var cord = toMidSystem.apply(Cord.at(x, y));
        return new Cell(cord, master);

    }
}
