package map;

import world.Base;
import world.Cord;
import world.ITerrainMap;

import java.util.List;

public interface IBaseGenerator {

    List<Base<Cord>> generateForSection(ITerrainMap<Cord> sectionMap);
}
