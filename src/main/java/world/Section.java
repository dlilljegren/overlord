package world;

import changes.SectionUpdate;
import changes.SectionUpdateBuilder;
import com.google.common.collect.Maps;
import map.IBaseGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.map.mutable.MapAdapter;
import world.exceptions.AddUnitException;
import world.exceptions.RemoveUnitException;
import world.exceptions.SectionException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
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


    private final WorldDefinition worldDefinition;
    private final SectionDefinition sectionDefinition;


    public final SectorNeighbours neighbours;

    private final MutableMap<Cord, Unit> units = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<Cord, Terrain> terrains;

    private final Map<Cord, Base<Cord>> bases;

    private final Map<Cord, Cell> cells;

    private final Map<Cord, Base<Cord>> cordToBase;

    private final Map<Integer, SectionNeighbour> sectionNeighbours;

    private SectionVersion version;

    private SectionUpdate.Snapshot lastSnapshot;

    private final Predicate<Cord> isSectionMaster;

    private final IntSupplier nextNewUnits;

    private static final Logger L = LogManager.getLogger(Section.class);


    private final IZoneOfControlCalculator zoc;

    public Section(int sectionNo, WorldDefinition worldDefinition, ITerrainMap<Cord> map, IBaseGenerator baseGenerator) {
        assert sectionNo >= 0;
        assert worldDefinition != null;
        assert map != null;
        assert baseGenerator != null;

        this.sectionNo = sectionNo;
        this.worldDefinition = worldDefinition;
        this.sectionDefinition = worldDefinition.sectionDefinition;
        this.version = SectionVersion.initial(sectionNo);

        this.terrains = map.terrainMap();
        assert this.terrains.keySet().stream().allMatch(worldDefinition.sectionDefinition.inSection) : this.terrains.keySet().stream().filter(worldDefinition.sectionDefinition.inSection.negate()).findFirst().get();

        List<Base<Cord>> baseList = baseGenerator.generateForSection(map, sectionNo);
        this.bases = baseList.stream().collect(toUnmodifiableMap(b -> b.center, Function.identity()));

        //L.debug("Created [{}] bases",baseList.size());


        //Check that all bases are completely within the area of the master rectangle
        assert this.bases.values().stream().flatMap(b -> b.area.stream()).allMatch(worldDefinition.section.isSectionMaster(sectionNo)) : format("Section:[%d] Base centre:[%s] first bad area:[%s]", sectionNo, this.bases.keySet(),
                this.bases.values().stream().flatMap(b -> b.area.stream()).filter(worldDefinition.section.isSectionMaster(sectionNo).negate()).findFirst().get());




        this.neighbours = SectorNeighbours.create(worldDefinition, sectionNo);


        this.sectionNeighbours = Maps.newHashMap();

        SectorCellCalculator scc = new SectorCellCalculator(sectionNo, worldDefinition);
        this.cells = scc.cells().collect(toUnmodifiableMap(c -> c.cord, Function.identity()));

        this.isSectionMaster = worldDefinition.section.isSectionMaster(sectionNo);

        this.nextNewUnits = worldDefinition.rules::unitsAllocatedPerSection;


        Function<Base<Cord>, Stream<Map.Entry<Cord, Base>>> baseToStream = b -> b.area.stream().map(c -> Maps.immutableEntry(c, b));
        this.cordToBase = bases.values().stream().flatMap(baseToStream).collect(toUnmodifiableMap(e -> e.getKey(), e -> e.getValue()));

        zoc = new ZoneOfControlCalculator2(sectionNo, worldDefinition);


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
        addUnitAtCord(unit, cord);
        this.version = currentVersion().next();
    }

    public Unit tryRemoveUnitAtCord(Team team, Cord cord) {
        isNotOccupiedOrThrow(cord);
        isSectionMasterOrThrow(cord);

        var unit = units.get(cord);
        assert unit != null;

        if (!unit.team.equals(team)) {
            throw new RemoveUnitException(sectionNo, cord, format("Can't remove unit at cord [%s] as it belongs to team [%s] while the request was made for team [%s]", cord, unit.team, team));
        }

        var removedUnit = this.removeUnitAtCord(unit, cord);
        if (removedUnit != null) {
            this.version = currentVersion().next();
        }
        return removedUnit;
    }

    public boolean isOccupied(Cord cord) {
        return units.containsKey(cord) || terrains.containsKey(cord) || cordToBase.containsKey(cord);
    }

    public boolean isFree(Cord cord) {
        return !isOccupied(cord);
    }

    private void isNotOccupiedOrThrow(Cord cord) {
        if (!units.containsKey(cord)) {
            throw new RemoveUnitException(sectionNo, cord, format("No unit exist at cord %s", cord));
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
        if (!this.zoc.hasControl(unit.team, cord)) {
            throw new AddUnitException.NotInZocException(this.sectionNo, cord, unit.team);
        }
    }

    private void isSectionMasterOrThrow(Cord cord) {
        if (!isSectionMaster.test(cord)) {
            throw new RuntimeException(format("Cord:[%s] is not owned by sectionNo [%s]", cord, this));
        }
    }



    private SectionNeighbour neighbour(int sectionNo) {
        return sectionNeighbours.computeIfAbsent(sectionNo, SectionNeighbour::new);
    }

    public Map<Integer, Cord> slavesAtCord(Cord cord) {
        @SuppressWarnings("UnstableApiUsage") final Map<Integer, Cord> collect = this.neighbours.slaves().stream()
                .map(this::neighbour)
                .filter(n -> n.isValid(cord))
                .collect(toImmutableMap(n -> n.sectionNo, n -> n.translate(cord)));
        return collect;
    }

    private void inSectionOrThrow(Cord cord) {
        if (!this.sectionDefinition.inSection(cord))
            throw new SectionException(sectionNo, format("Cord:[%s] outside sectionNo:[%d]", cord, sectionNo));
    }


    public int getMasterForCord(Cord cord) {

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
        var neighbourCord = worldDefinition.section.fromTo(this.sectionNo, neighbour).apply(cord);
        assert this.worldDefinition.section.inSection(neighbourCord) : format("[%s] is not a valid section cord in section:[%d]. Translated from [%s] in master section [%d]", neighbourCord, neighbour, cord, this.sectionNo);
        return neighbourCord;
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

        //ToDo add zoc
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

    /**
     * @param unit
     * @param cord
     */
    void addUnitAtCord(Unit unit, Cord cord) {
        inSectionOrThrow(cord);
        units.put(cord, unit);
        zoc.addUnit(unit, cord);
        version = version.next();
    }

    private Unit removeUnitAtCord(Unit unit, Cord cord) {
        var removed = units.remove(cord);
        assert removed != null : format("No unit at cord:[%s] expected:[%s] in section:[%d]", cord, unit, sectionNo);
        assert removed.equals(unit);
        zoc.removeUnit(unit, cord);
        return removed;
    }

    /**
     * Create units surrounding all free zoc for the given team
     *
     * @param base
     * @param team
     * @return the newly created units
     */
    public MapIterable<Cord, Unit> populateBase(Base<Cord> base, Team team) {
        var unit = Unit.forTeam(team);
        var newUnits = base.zoneOfControl
                .stream()
                .filter(isSectionMaster)
                .filter(this::isFree)
                .collect(toUnmodifiableMap(Function.identity(), c -> unit));
        L.info("Added [{}] units around base", newUnits.size());
        units.putAll(newUnits);

        zoc.addUnits(newUnits);

        return MapAdapter.adapt(newUnits);
    }

    public MapIterable<Cord, Unit> populateRandomBase(Team owner) {
        if (bases.size() == 0) return org.eclipse.collections.impl.factory.Maps.immutable.empty();

        Random r = new Random();
        var random = r.nextInt(Math.max(1, this.bases.size() - 1));
        var base = this.bases.values().stream().skip(random).findFirst();

        var result = base.map(b -> populateBase(b, owner));


        this.version = currentVersion().next();

        return result.orElseGet(org.eclipse.collections.impl.factory.Maps.immutable::empty);

    }


    public void slaveAddUnitAtCord(Unit unit, Cord cord) {
        assert !worldDefinition.section.isSectionMaster(sectionNo).test(cord) : "Can only slave add cords outside the section's master rectangle";
        this.addUnitAtCord(unit, cord);
        this.version = currentVersion().next();
    }

    public void slaveRemoveUnitAtCord(Unit unit, Cord cord) {
        assert !worldDefinition.section.isSectionMaster(sectionNo).test(cord) : "Can only slave add cords outside the section's master rectangle";
        this.removeUnitAtCord(unit, cord);
        this.version = currentVersion().next();
    }

    public Unit getUnit(Cord cord) {
        return units.get(cord);
    }

    public ZocResult getCurrentZoc() {
        return zoc.getSnapshot();
    }

    public ZocResult getDeltaZoc() {
        return zoc.getDelta();
    }


    /**
     * ToDo get rid of this
     */
    class SectionNeighbour implements Comparable<SectionNeighbour> {

        final int sectionNo;

        final Function<Cord, Cord> sectionToNeighbour;

        SectionNeighbour(int sectionNo) {
            this.sectionNo = sectionNo;

            this.sectionToNeighbour = Functions.chain(worldDefinition.section.toWorld(Section.this.sectionNo), worldDefinition.world.toSection(sectionNo));

        }

        boolean isValid(Cord cordInThisSection) {
            Cord sc = translate(cordInThisSection);
            return worldDefinition.sectionDefinition.inSection(sc);
        }

        Cord translate(Cord cordInThisSection) {
            return this.sectionToNeighbour.apply(cordInThisSection);
        }

        @Override
        public int compareTo(@Nonnull SectionNeighbour o) {
            return Integer.compare(sectionNo, o.sectionNo);
        }
    }
}
