package world;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;


public class Base<C extends ICord> {
    public final C center;
    public final int radius;
    public final Set<C> zoneOfControl;
    public final Set<C> area;

    public final String name;
    public static final AtomicInteger namer = new AtomicInteger(1);

    transient private final Predicate<C> areaPredicate;

    public Base(String name, C center, int radius, Set<C> area, Set<C> zoneOfControl) {
        this.center = center;
        this.radius = radius;
        this.zoneOfControl = zoneOfControl;
        this.area = area;

        this.areaPredicate = c -> area.contains(c);
        this.name = name;
    }


    public static Predicate<Cord> baseGeometry(SectionDefinition sd, Cord cord) {
        return sd.cross(cord);
    }

    public static Base<Cord> createInSection(Cord center, int radius, WorldDefinition worldDefinition) {
        var sectionDefinition = worldDefinition.sectionDefinition;
        var crossGeometry = baseGeometry(sectionDefinition, center);
        var zocGeometry = sectionDefinition.circleArea(center, radius).and(crossGeometry.negate());

        var zoc = worldDefinition.sectionDefinition.fillArea(zocGeometry).collect(toCollection(() -> Sets.newIdentityHashSet()));
        var area = sectionDefinition.fillArea(crossGeometry).collect(toCollection(() -> Sets.newIdentityHashSet()));
        var name = format("base-%s", namer.getAndAdd(1));
        return new Base(name, center, radius, area, zoc);
    }

    public Predicate<C> area() {
        return areaPredicate;
    }

    /**
     * Is this cord occupied by the base i.e. can't place unit on it
     *
     * @param cord
     * @return
     */
    public boolean isOccupied(Cord cord) {
        return area.contains(cord);
    }

    public boolean isInZoneOfContol(Cord cord) {
        return this.zoneOfControl.contains(cord);
    }

    public Optional<Team> owner(Map<Cord, Team> teams) {
        var res = teams.entrySet().stream().filter(e -> isInZoneOfContol(e.getKey())).collect(Collectors.groupingBy(e -> e.getValue(), Collectors.counting()));

        var max = res.values().stream().mapToLong(l -> l).max();
        if (!max.isPresent()) return Optional.empty();

        var res2 = res.entrySet().stream().filter(e -> e.getValue().equals(max.getAsLong()));

        return res2.findFirst().map(e -> e.getKey());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("center", center)
                .add("radius", radius)
                .add("area", area.size())
                .add("zoneOfControl", zoneOfControl.size())
                .toString();
    }
}
