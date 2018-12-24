package world;

import games.Rules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewBorderTest {

    private final WorldDefinition wd_3x3_5x5_2_1 = new WorldDefinition(3, 3, 2, 1, 27, new Rules() {
    }, SectionDefinition.create(5, 5, SectionDefinition.GridType.SQUARE));


    @Test
    void create_section_0() {
        final View view = wd_3x3_5x5_2_1.createViewAtUpperLeft(0, 3, 3);
        var underTest = ViewBorder.create(view, 0);

        assertEquals(Cord.at(-2, -2), underTest.borders.get(ViewBorder.BorderDirection.North).start);
        assertEquals(Cord.at(2, -2), underTest.borders.get(ViewBorder.BorderDirection.North).end);

        assertEquals(Cord.at(-2, 2), underTest.borders.get(ViewBorder.BorderDirection.South).start);
        assertEquals(Cord.at(2, 2), underTest.borders.get(ViewBorder.BorderDirection.South).end);

        assertEquals(Cord.at(-2, -2), underTest.borders.get(ViewBorder.BorderDirection.West).start);
        assertEquals(Cord.at(-2, 2), underTest.borders.get(ViewBorder.BorderDirection.West).end);

        assertEquals(Cord.at(2, -2), underTest.borders.get(ViewBorder.BorderDirection.East).start);
        assertEquals(Cord.at(2, 2), underTest.borders.get(ViewBorder.BorderDirection.East).end);

    }

    @Test
    void create_section_8() {
        final View view = wd_3x3_5x5_2_1.createViewAtUpperLeft(8, 3, 3);
        var underTest = ViewBorder.create(view, 8);

        assertEquals(Cord.at(-0, -1), underTest.borders.get(ViewBorder.BorderDirection.North).start);
        assertEquals(Cord.at(2, -1), underTest.borders.get(ViewBorder.BorderDirection.North).end);

        assertEquals(Cord.at(0, 2), underTest.borders.get(ViewBorder.BorderDirection.South).start);
        assertEquals(Cord.at(2, 2), underTest.borders.get(ViewBorder.BorderDirection.South).end);

        assertEquals(Cord.at(0, -1), underTest.borders.get(ViewBorder.BorderDirection.West).start);
        assertEquals(Cord.at(0, 2), underTest.borders.get(ViewBorder.BorderDirection.West).end);

        assertEquals(Cord.at(2, -1), underTest.borders.get(ViewBorder.BorderDirection.East).start);
        assertEquals(Cord.at(2, 2), underTest.borders.get(ViewBorder.BorderDirection.East).end);

    }
}