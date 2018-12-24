import world.ViewDefinition;

import java.util.UUID;

import static java.lang.String.format;

public class ActorNames {

    public static String sessionName(UUID uuid) {
        return sessionName(uuid.toString().substring(0, 5));
    }

    public static String sessionName(String uuid) {
        return format("session-%s", uuid);
    }

    public static String playerName(String name) {
        return format("plr-%s", name);
    }

    public static String sectionName(Integer sectionNo) {
        return format("Section-%d", sectionNo);
    }

    public static String viewName(ViewDefinition vd, int count) {
        return format("View-%d:%d-%dx%d-%d", vd.origo.col, vd.origo.row, vd.width, vd.height, count);
    }
}
