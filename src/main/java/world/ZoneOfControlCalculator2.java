package world;


import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.predicate.primitive.ObjectLongPredicate;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.ImmutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.primitive.ObjectIntPair;
import org.eclipse.collections.api.tuple.primitive.ObjectLongPair;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

public class ZoneOfControlCalculator2 implements IZoneOfControlCalculator {


    private final SectionDefinition sectionDefinition;

    private final MutableMap<Cord, CellControl2> zocs;

    private final int zocRadius;

    private final ImmutableObjectIntMap<Cord> cordToPoints;

    private final MutableSet<Cord> dirty;

    private final Predicate<Cord> isValid;
    private final Predicate<Cord> isInMaster;

    private final Function<Pair<?, CellControl2>, SetIterable<Team>> deltaLosers;
    private final Function<Pair<?, CellControl2>, SetIterable<Team>> deltaWinners;
    private final Function<Pair<?, CellControl2>, SetIterable<Team>> currentWinners;

    private final ObjectLongPredicate<Cord> isMasterCord;

    public ZoneOfControlCalculator2(int sectionNo, @Nonnull WorldDefinition worldDefinition) {
        this.sectionDefinition = worldDefinition.sectionDefinition;

        this.zocs = Maps.mutable.ofInitialCapacity(worldDefinition.sectionDefinition.area() / 2);

        this.zocRadius = worldDefinition.rules.zocRadius();
        this.isValid = worldDefinition.sectionDefinition.inSection;

        ImmutableSet<Cord> area = worldDefinition.section.circle(zocRadius);


        ToIntFunction<Cord> pointFunction = c -> 1 + zocRadius - sectionDefinition.distance(Cord.ZERO, c);
        MutableObjectIntMap<Cord> tmp = ObjectIntMaps.mutable.empty();
        area.collect(c -> PrimitiveTuples.pair(c, pointFunction.applyAsInt(c))).forEach(tmp::putPair);
        cordToPoints = tmp.toImmutable();

        this.dirty = Sets.mutable.empty();

        this.isInMaster = worldDefinition.section.isSectionMaster(sectionNo);

        this.deltaLosers = Functions.chain(Functions.secondOfPair(), CellControl2::teamsLoosingZoc);
        this.deltaWinners = Functions.chain(Functions.secondOfPair(), CellControl2::teamsGainingZoc);
        this.currentWinners = Functions.chain(Functions.secondOfPair(), CellControl2::currentWinners);

        this.isMasterCord = (c, p) -> isInMaster.test(c);
    }


    private CellControl2 at(Cord cord) {
        return this.zocs.getIfAbsentPut(cord, CellControl2::new);
    }



    private void markAsDirty(Cord c) {
        this.dirty.add(c);
    }


    @Override
    public ZocResult getSnapshot() {
        this.zocs.forEach(CellControl2::recalculateSnapshot);
        return ZocResult.snapshot(currentWinners(zocs));
    }

    /**
     * The delta should be all changes since the last call to the delta function
     * Two things might have happened, a team may have gained control of a cord or it may have lost control
     * In both cases only cords "touched" needs to be considered
     *
     * @return the cords teams have gained or lost control over
     */
    @Override
    public ZocResult getDelta() {
        var dirty = zocs.select(this::isDirty);//Get all that have changed
        dirty.forEach(CellControl2::recalculateDelta);//Recalculate the current winner
        var result = ZocResult.delta(deltaWinners(dirty), deltaLosers(dirty)); //Extract the delta
        dirty.clear(); //Clear all dirty
        return result;
    }

    @Override
    public void addUnit(Unit unit, Cord at) {
        addPoints(at, unit);
    }

    @Override
    public void removeUnit(Unit unit, Cord at) {
        removePoints(at, unit);
    }

    @Override
    public void addUnits(Map<Cord, Unit> units) {
        units.forEach(this::addPoints);
    }

    @Override
    public void removeUnits(Map<Cord, Unit> units) {
        units.forEach(this::removePoints);
    }

    @Override
    public boolean hasControl(Team team, Cord cord) {
        var cc = zocs.get(cord);
        if (cc == null) return false;
        if (dirty.contains(cord)) {
            cc.recalculate(true);
        }
        return cc.hasControl(team);
    }


    private Map<Team, Set<Cord>> deltaWinners(@Nonnull MapIterable<Cord, CellControl2> zoc) {
        return extract(zoc, this.deltaWinners);
    }

    private Map<Team, Set<Cord>> deltaLosers(@Nonnull MapIterable<Cord, CellControl2> zoc) {
        return extract(zoc, this.deltaLosers);
    }

    private Map<Team, Set<Cord>> currentWinners(@Nonnull MapIterable<Cord, CellControl2> zoc) {
        return extract(zoc, this.currentWinners);
    }

    private Map<Team, Set<Cord>> extract(@Nonnull MapIterable<Cord, CellControl2> zoc, Function<Pair<?, CellControl2>, SetIterable<Team>> grouper) {
        return zoc
                .keyValuesView()
                .groupByEach(grouper)
                .collectValues(Pair::getOne)
                .rejectKeysValues(this::isNotOwnBySection)
                .toMap(Sets.mutable::empty);
    }


    private boolean isNotOwnBySection(Team team, Cord cord) {
        return !isInMaster.test(cord);
    }



    private boolean isDirty(Cord cord, @SuppressWarnings("unused") CellControl2 dummy) {
        return this.dirty.contains(cord);
    }

    private void addPoints(Cord cord, Unit unit) {
        influence(cord, unit, 1);
    }

    private void removePoints(Cord cord, Unit unit) {
        influence(cord,unit,-1);
    }

    private void influence(Cord cord, Unit unit, int factor) {
        //Use the pre-calculated map to find all cords affected
        cordToPoints.keyValuesView()
                .sumByInt(p -> p.getOne().add(cord), ObjectIntPair::getTwo)
                //.reject(isMasterCord)
                .keyValuesView()
                .forEach(p -> {
                    var c = p.getOne();
                    var points = p.getTwo();
                    at(c).influence(unit, (int) points * factor);
                    markAsDirty(c);
                });
    }

    /**
     * Check if the Cord is not outside of sections general border
     *
     * @param cordObjectIntPair
     * @return
     */
    private boolean isValidCord(ObjectLongPair<Cord> cordObjectIntPair) {
        return isValid.accept(cordObjectIntPair.getOne());
    }






    private class CellControl2 {

        private final MutableObjectIntMap<Team> teamInfluence = ObjectIntMaps.mutable.empty();
        private SetIterable<Team> currentWinners;
        private SetIterable<Team> lastWinners;
        private SetIterable<Team> deltaLosers;
        private SetIterable<Team> deltaWinner;

        private CellControl2() {
            clear();
        }

        private void influence(@Nonnull Unit unit, int points) {
            int newValue = teamInfluence.addToValue(unit.team, points);
            if (newValue == 0) {
                teamInfluence.removeKey(unit.team);
            }

        }

        private void recalculateDelta() {
            recalculate(false);
        }

        private void recalculateSnapshot() {
            recalculate(true);
        }

        private void recalculate(boolean snapshot) {
            long allPoints = teamInfluence.sum();
            var newWinners = teamInfluence.select((t, p) -> p * 100 / allPoints > 30).keysView().toSet();

            if (!snapshot) {
                //Calculate the losers
                deltaLosers = lastWinners.difference(newWinners);
                deltaWinner = newWinners.difference(lastWinners);
                lastWinners = newWinners;
            }
            currentWinners = newWinners;

        }

        private SetIterable<Team> currentWinners() {
            return currentWinners;
        }

        private SetIterable<Team> teamsGainingZoc() {
            return deltaWinner;
        }

        private SetIterable<Team> teamsLoosingZoc() {
            return deltaLosers;
        }


        private void clear(){
            this.deltaLosers = Sets.immutable.empty();
            this.deltaWinner = Sets.immutable.empty();
            this.lastWinners = Sets.immutable.empty();
        }

        public boolean hasControl(Team team) {

            return currentWinners.contains(team);
        }
    }
}
