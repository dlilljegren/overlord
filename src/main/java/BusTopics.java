import world.Player;

import static java.lang.String.format;

public abstract class BusTopics {


    public static String sectorBroadcast(int sectorNo) {
        return format("sectionNo-bc-%d", sectorNo);
    }

    public static String sectorCursorBroadcast(int sectorNo) {
        return format("sectionNo-cursor-bc-%d", sectorNo);
    }

    public static String playerBroadcast(Player player) {
        return format("playerActor-%s", player.name);
    }
}
