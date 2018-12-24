package extensions.java.util.Set;


import world.Cord;
import world.Hex;
import world.SectionDefinition;

import java.util.Set;
import java.util.stream.Collectors;


public class HexExtension {


    public static Set<Cord> toCord(Set<Hex> hexes, SectionDefinition.Hex sd) {
        return hexes.stream().map(h -> sd.toCord(h)).collect(Collectors.toUnmodifiableSet());
    }
}
