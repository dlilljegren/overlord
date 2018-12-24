package world;

import org.apache.commons.text.WordUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Teams {

    private static ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap();

    public static final Team RED = teamForName("Red");
    public static final Team Blue = teamForName("Blue");

    public static Team None = new NoTeam();


    public static Team teamForName(String name) {
        Objects.requireNonNull(name);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Bad team-name:" + name);
        }
        name = name.toLowerCase();
        final var fName = WordUtils.capitalize(name);

        if (name.equalsIgnoreCase("none")) return None;
        return teams.computeIfAbsent(name, n -> new ColorTeam(fName));
    }
}
