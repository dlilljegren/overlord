package world;

import games.Rules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SectionCellCalculatorTest {

    final SectionDefinition sectionDefinition = SectionDefinition.create(5, 5, SectionDefinition.GridType.SQUARE);
    final WorldDefinition worldDefinition = new WorldDefinition(3, 3, 1, 1, 27, new Rules() {
    }, sectionDefinition);


    @Test
    void cells() {


        SectorCellCalculator underTest0 = new SectorCellCalculator(0, worldDefinition);

        //0 is the master of all
        underTest0.cells().mapToInt(c -> c.master).forEach(m -> assertEquals(0, m));
        //0 has all cells
        assertEquals(worldDefinition.sectionDefinition.area(), underTest0.cells().count());

        SectorCellCalculator underTest1 = new SectorCellCalculator(1, worldDefinition);

        //1 all cells where col<overlap belongs to 0

        underTest1.cells().forEach(System.out::println);
        underTest1.cells().filter(c -> c.cord.col < worldDefinition.colSectionOverlap).forEach(m -> assertEquals(0, m.master, m.toString()));

        underTest1.cells().filter(c -> c.cord.col >= worldDefinition.colSectionOverlap).forEach(m -> assertEquals(1, m.master, m.toString()));


        assertEquals(false, underTest1.cells().filter(c -> worldDefinition.masterRectangle(1).test(c.cord)).filter(c -> c.master != 1).findFirst().isPresent());


    }
}