package world;

import games.Rules;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static world.SectionDefinition.GridType;
import static world.SectionDefinition.create;

class SectionNeighboursTest {


    @Test
    public void NeighbourTest() {
        WorldDefinition wd = new WorldDefinition(3, 2, 10, 10, 27, new Rules() {
        }, create(21, 21, GridType.SQUARE));

        SectorNeighbours underTest = SectorNeighbours.create(wd, 4);

        assertEquals(Set.of(5), underTest.slaves());
        assertEquals(Set.of(0, 1, 3), underTest.masters());
        assertEquals(Set.of(2), underTest.normals());
    }

    @Test
    public void NeighbourTest2() {
        WorldDefinition wd = new WorldDefinition(3, 3, 10, 10, 27, new Rules() {
        }, create(21, 21, GridType.SQUARE));

        SectorNeighbours underTest = SectorNeighbours.create(wd, 4);

        assertEquals(Set.of(5, 7, 8), underTest.slaves());
        assertEquals(Set.of(0, 1, 3), underTest.masters());
        assertEquals(Set.of(2, 6), underTest.normals());
    }
}