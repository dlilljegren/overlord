package world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class ViewBorder {

    public final Map<BorderDirection, BorderLine> borders;

    public enum BorderDirection {
        North, South, West, East
    }

    public ViewBorder(Map<BorderDirection, BorderLine> borders) {
        this.borders = borders;
    }


    public static ViewBorder create(View view, int section) {
        var masterRect = view.masterRectangle(section);


        var map = Maps.immutableEnumMap(ImmutableMap.of(
                BorderDirection.North, new BorderLine(masterRect.NW, masterRect.NE),
                BorderDirection.South, new BorderLine(masterRect.SW, masterRect.SE),
                BorderDirection.West, new BorderLine(masterRect.NW, masterRect.SW),
                BorderDirection.East, new BorderLine(masterRect.NE, masterRect.SE)

        ));
        return new ViewBorder(map);
    }

    public static class BorderLine {
        public final WorldCord start;
        public final WorldCord end;

        public BorderLine(WorldCord start, WorldCord end) {
            this.start = start;
            this.end = end;
        }
    }


}
