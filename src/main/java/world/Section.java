package world;

import changes.SectionUpdate;
import changes.SectionUpdateBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import map.IBaseGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class Section {
    public final int sectionNo;
    private final int row;
    private final int column;

    private final WorldDefinition worldDefinition;
    private final SectionDefinition sectionDefinition;


    public final SectorNeighbours neighbours;

    private final Map<Cord, Unit> units = Maps.newHashMap();
    private final Map<Cord, Terrain> terrains;

    final Map<Cord, Base<Cord>> bases;

    final Map<Cord, Cell> cells;

    private final Map<Cord, Base<Cord>> cordToBase;

    private final Map<Integer, SectionNeighbour> sectionNeighbours;

    private SectionVersion version;

    private SectionUpdate.Snapshot lastSnapshot;

    private final Predicate<Cord> isSectionMaster;

    private final IntSupplier nextNewUnits;

    private static final Logger L = LogManager.getLogger(Section.class);


    public Section(int sectionNo, WorldDefinition worldDefinition, ITerrainMap<Cord> map, IBaseGenerator baseGenerator) {
        assert sectionNo >= 0;
        assert worldDefinition != null;
        assert map != null;
        assert baseGenerator != null;

        this.version = SectionVersion.initial(sectionNo);

        this.terrains = map.terrainMap();
        assert this.terrains.keySet().stream().allMatch(worldDefinition.sectionDefinition.inSection) : this.terrains.keySet().stream().filter(worldDefinition.sectionDefinition.inSection.negate()).findFirst().get();

        List<Base<Cord>> baseList = baseGenerator.generateForSection(map);
        this.bases = baseList.stream().collect(toUnmodifiableMap(b -> b.center, Function.identity()));

        //L.debug("Created [{}] bases",baseList.size());

        assert this.bases.keySet().stream().allMatch(worldDefinition.sectionDefinition.inSection) : format("Base centre: %s", this.bases.keySet());


        this.sectionNo = sectionNo;
        this.worldDefinition = worldDefinition;
        this.sectionDefinition = worldDefinition.sectionDefinition;

        this.neighbours = SectorNeighbours.create(worldDefinition, sectionNo);

        this.row = sectionNo / worldDefinition.gridWidth;
        this.column = sectionNo % worldDefinition.gridWidth;

        this.sectionNeighbours = Maps.newHashMap();

        SectorCellCalculator scc = new SectorCellCalculator(sectionNo, worldDefinition);
        this.cells = scc.cells().collect(toUnmodifiableMap(c -> c.cord, Function.identity()));

        this.isSectionMaster = sc -> cells.get(sc).master == sectionNo;

        this.nextNewUnits = worldDefinition.rules::unitsAllocatedPerSection;


        Function<Base<Cord>, Stream<Map.Entry<Cord, Base>>> baseToStream = b -> b.area.stream().map(c -> Maps.immutableEntry(c, b));
        this.cordToBase = bases.values().stream().flatMap(baseToStream).collect(toUnmodifiableMap(e -> e.getKey(), e -> e.getValue()));


        this.bases.values().stream().findFirst().ifPresent(b -> populateBase(b, Teams.teamForName("red")));

        //Keep at end
        var initialSnapshot = new SectionUpdateBuilder();
        units.forEach(initialSnapshot::add);
        terrains.forEach(initialSnapshot::add);
        bases.forEach(initialSnapshot::add);
        this.lastSnapshot = initialSnapshot.buildSnapshot(version);
    }


    @Override
    public String toString() {
        return format("Section-%d", sectionNo);
    }

    public void tryAddUnitAtCord(Unit unit, Cord cord) {
        isOccupiedOrThrow(cord);
        isSectionMasterOrThrow(cord);
        isControlledOrThrow(cord, unit);
        setUnitAtCord(unit, cord);
    }

    public Unit tryRemoveUnitAtCord(Team team, Cord cord) {
        isNotOccupiedOrThrow(cord);
        isSectionMasterOrThrow(cord);

        var unit = units.get(cord);
        assert unit != null;

        if (!unit.team.equals(team)) {
            throw new RuntimeException(format("Can't remove unit at cord [%s] as it belongs to team [%s] while the request was made for team [%s]", cord, unit.team, team));
        }

        return this.removeUnitAtCord(unit, cord);
    }

    public boolean isOccupied(Cord cord) {
        return units.containsKey(cord) || terrains.containsKey(cord) || cordToBase.containsKey(cord);
    }

    public boolean isFree(Cord cord) {
        return !isOccupied(cord);
    }

    private void isNotOccupiedOrThrow(Cord cord) {
        if (!units.containsKey(cord)) {
            throw new RuntimeException(format("No unit exist at cord %s", cord));
        }
    }

    private void isOccupiedOrThrow(Cord cord) {
        if (units.containsKey(cord)) {
            throw new AddUnitException.OccupiedByUnitException(this.sectionNo, cord, this.units.get(cord));
        }
        if (terrains.containsKey(cord)) {
            throw new AddUnitException.OccupiedByTerrainException(this.sectionNo, cord, this.terrains.get(cord));
        }
        if (cordToBase.containsKey(cord)) {
            throw new AddUnitException.OccupiedByBaseException(this.sectionNo, cord, cordToBase.get(cord));
        }
    }


    /**
     * Check that the cord is in a controlled area
     *
     * @param cord
     * @param unit
     */
    private void isControlledOrThrow(Cord cord, Unit unit) {
        //Ask for a geometry around the cord
        //Use the geometry to filter out all units that may affect the cord
        //Based on the units calculate the zone of control they have on the given cord

        var geometry = sectionDefinition.circleArea(cord, worldDefinition.rules.zocRadius());
        var maxDistance = worldDefinition.rules.zocRadius();
        var zoneOfControl = new ZoneOfControlAtCordCalculator(cord, sectionDefinition, units, Optional.of(geometry), maxDistance);
        var cellControl = zoneOfControl.cordControl();

        Runnable noZoc = () -> {
            throw new AddUnitException.NotInZocException(this.sectionNo, cord, unit.team);
        };

        cellControl.ifPresentOrElse(
                cc -> {
                    if (!worldDefinition.rules.teamCanAdd(cc, unit.team)) {
                        if (cc.bestTeam().isPresent())
                            throw new AddUnitException.NotInZocException(sectionNo, cord, unit.team, cc.bestTeam().get());
                        else
                            noZoc.run();
                    }
                }, noZoc
        );

    }

    private void isSectionMasterOrThrow(Cord cord) {
        if (!isSectionMaster.test(cord)) {
            throw new RuntimeException(format("Cord:[%s] is not owned by sectionNo [%s]", cord, this));
        }
    }

    public void setUnitAtCord(Unit unit, Cord cord) {
        inSectionOrThrow(cord);
        units.put(cord, unit);
        version = version.next();
    }

    private SectionNeighbour neighbour(int sectionNo) {
        return sectionNeighbours.computeIfAbsent(sectionNo, SectionNeighbour::new);
    }

    public Map<Integer, Cord> slavesAtCord(Cord cord) {
        final Map<Integer, Cord> collect = this.neighbours.slaves().stream()
                .map(this::neighbour)
                .filter(n -> n.isValid(cord))
                .collect(toImmutableMap(n -> n.sectionNo, n -> n.translate(cord)));
        return collect;
    }

    private void inSectionOrThrow(Cord cord) {
        if (!this.sectionDefinition.inSection(cord))
            throw new RuntimeException(format("Cord %s outside sectionNo"));
    }


    public int getMasterForCord(Cord cord) {
        //Transform to cord space of potential masters
        //If we find a valid master ( picking the lowest in case there are many ) return masters id
        //Otherwise this sectionNo is the master

        /*
        final Integer master = neighbours.masters().stream()
                .map(this::neighbour)
                .filter(n -> n.isValid(cord))
                .sorted()
                .findFirst()
                .map(sectionNeighbour -> sectionNeighbour.sectionNo)
                .orElse(this.sectionNo);
        return
                master;
                */
        //assert worldDefinition.sectionDefinition.inSectionOrThrow(cord) : cord;
        if (!this.worldDefinition.sectionDefinition.inSection(cord)) {
            throw new RuntimeException(format("Invalid cord:%s", cord));
        }
        assert cells.containsKey(cord) : "No cell for cord:" + cord;

        return cells.get(cord).master;
    }


    private SectionVersion currentVersion() {
        return version;
    }

    public Cord translate(Cord cord, int neighbour) {
        return neighbour(neighbour).translate(cord);
    }

    public SectorCombatResult combat() {
        Combat combat = new Combat(this.units, this.isSectionMaster, worldDefinition.sectionDefinition::neighbours);
        this.version = currentVersion().next();

        final SectorCombatResult combatResult = combat.combat(version);
        applyChange(combatResult.delta);

        return combatResult;

    }

    public Map<Player, Integer> newUnitAllocation(Stream<Player> players) {
        var unitDivider = new SectorUnitAllocation(this.units, this.isSectionMaster, worldDefinition.rules.unitsAllocatedPerSection());

        var perPlayer = unitDivider.allocatePerPlayer(players);
        return perPlayer;
    }

    public Map<Player, Integer> baseUnitAllocation(Stream<Player> players) {
        var unitAllocator = new BaseUnitAllocation(units, bases.values(), worldDefinition.rules.unitsAllocatedPerBase());
        return unitAllocator.allocatePerPlayer(players);

    }


    private void applyChange(SectionUpdate.Snapshot snapshot) {
        //this.terrains = snapshot.terrains.terrainMap();
        //this.bases = snapshot.bases.baseMap();
        this.units.clear();
        this.units.putAll(snapshot.units);

        this.version = snapshot.sectionVersion;
    }

    private void applyChange(SectionUpdate.Delta delta) {
        this.units.putAll(delta.units);

        var badAndGood = delta.unitDeletes.stream().collect(groupingBy(this.units::containsKey));

        assert !badAndGood.containsKey(Boolean.FALSE);
        badAndGood.getOrDefault(Boolean.TRUE, Collections.EMPTY_LIST).forEach(this.units::remove);

        this.version = delta.sectionVersion;
    }


    public SectionUpdate.Snapshot snapshot() {
        if (lastSnapshot.sectionVersion.version != version.version) {
            var cb = new SectionUpdateBuilder();
            units.forEach(cb::add);
            terrains.forEach(cb::add);
            bases.forEach(cb::add);
            this.lastSnapshot = cb.buildSnapshot(version);
        }
        return this.lastSnapshot;
    }

    public Stream<CellControl> calculateZoneOfControl() {
        ZoneOfControlCalculator zoneOfControlCalculator = new ZoneOfControlCalculator(this.worldDefinition.sectionDefinition, this.units, Optional.empty());
        return zoneOfControlCalculator.result();
    }

    public Unit removeUnitAtCord(Unit unit, Cord cord) {
        var removed = units.remove(cord);
        assert removed.equals(unit);
        return removed;
    }

    /**
     * Create units surrounding all free zoc for the given team
     *
     * @param base
     * @param team
     * @return the newly created units
     */
    public Map<Cord, Unit> populateBase(Base<Cord> base, Team team) {
        Set<Cord> freeZoc = Sets.filter(base.zoneOfControl, this::isFree);
        var unit = Unit.forTeam(team);

        var newUnits = freeZoc.stream().collect(toUnmodifiableMap(Function.identity(), c -> unit));
        L.info("Added [{}] units around base", newUnits.size());
        units.putAll(newUnits);
        return newUnits;
    }


    class SectionNeighbour implements Comparable<SectionNeighbour> {

        final int sectionNo;
        final int translateX;
        final int translateY;

        SectionNeighbour(int sectionNo) {
            this.sectionNo = sectionNo;


            this.translateX = worldDefinition.translateColumn(Section.this.sectionNo, sectionNo);
            this.translateY = worldDefinition.translateRow(Section.this.sectionNo, sectionNo);
        }

        boolean isValid(Cord cordInThisSection) {
            Cord sc = translate(cordInThisSection);
            return worldDefinition.sectionDefinition.inSection(sc);
        }

        Cord translate(Cord cordInThisSection) {
            return Cord.at(
                    cordInThisSection.col + translateX,
                    cordInThisSection.row + translateY);
        }

        @Override
        public int compareTo(SectionNeighbour o) {
            return Integer.compare(sectionNo, o.sectionNo);
        }
    }
}
