package world;

import games.Rules;
import map.BasicBaseGenerator;
import org.junit.jupiter.api.Test;

class SectionTest {


    final WorldDefinition worldDefinition = new WorldDefinition(2, 2, 2, 2, 27, new Rules() {
    }, SectionDefinition.create(11, 11, SectionDefinition.GridType.SQUARE));

    final BasicBaseGenerator baseGenerator = new BasicBaseGenerator(worldDefinition, 2, 2);


    Unit Red = Unit.forTeam("Red");
    Unit Blue = Unit.forTeam("Blue");

    @Test
    public void combatTest() {


        Section underTest = new Section(0, worldDefinition, ITerrainMap.EMPTY, baseGenerator);

        //underTest.addUnitAtCord(Red,Cord.at(0,0));
        underTest.addUnitAtCord(Red, Cord.at(0, 1));
        underTest.addUnitAtCord(Red, Cord.at(0, 2));
        underTest.addUnitAtCord(Red, Cord.at(0, 3));

        underTest.addUnitAtCord(Blue, Cord.at(1, 0));
        underTest.addUnitAtCord(Blue, Cord.at(1, 1));
        underTest.addUnitAtCord(Blue, Cord.at(1, 2));
        underTest.addUnitAtCord(Blue, Cord.at(1, 3));


        underTest.combat().delta.unitDeletes.forEach(System.out::println);

    }
}