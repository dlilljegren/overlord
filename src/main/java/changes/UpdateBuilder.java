package changes;

import com.google.common.collect.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

abstract class UpdateBuilder<C extends ICord> {
    private static final Logger L = LogManager.getLogger(UpdateBuilder.class);

    public final Map<Class, Entry<?>> entries;
    private final Map<C, Object> cords;


    public UpdateBuilder() {


        entries = ImmutableMap.of(
                Unit.class, new Entry<>(),
                Terrain.class, new EntryNoRemove<>(),
                Base.class, new EntryNoRemove<>()
        );
        this.cords = Maps.newHashMap();
    }

    public void clear() {
        entries.values().stream().forEach(Entry::clear);
        this.cords.clear();
    }


    private <T> Entry<T> forClass(Class<T> klass) {
        return (Entry<T>) entries.get(klass);
    }

    private <T, V> List<V> adds(Class<T> klass, Function<Place<T>, V> f) {
        return forClass(klass).added.stream().map(f).collect(Collectors.toUnmodifiableList());
    }

    protected <T> Map<C, T> buildMap(Class<T> clazz) {
        return forClass(clazz).added.stream().collect(Collectors.toUnmodifiableMap(p -> p.at, p -> p.obj));
    }

    protected Map<C, Base<C>> buildBaseMap() {
        return forClass(Base.class).added.stream().collect(Collectors.toUnmodifiableMap(p -> p.at, p -> p.obj));
    }

    public void removeUnit(C cord) {
        forClass(Unit.class).remove(cord);
    }

    public void add(C cord, Unit unit) {
        forClass(Unit.class).add(unit, cord);
    }

    public void add(C cord, Terrain terrain) {
        forClass(Terrain.class).add(terrain, cord);
    }

    public void add(C cord, Base<C> base) {
        forClass(Base.class).add(base, cord);
    }


    public boolean isOccupied(Cord cord) {
        return cords.containsKey(cord);
    }

    public boolean isOccupied(Predicate<? super ICord> predicate) {
        return cords.keySet().stream().anyMatch(predicate);
    }

    public boolean areFree(Set<? extends ICord> cells) {
        return Sets.intersection(cords.keySet(), cells).isEmpty();
    }


    protected Collection<C> removedUnits() {
        return forClass(Unit.class).removed();
    }


    private class Entry<T> implements IEntry<T, C> {
        private final List<Place<T>> added;
        private ImmutableSet.Builder<C> removed;

        Entry() {
            this.added = Lists.newArrayList();
            this.removed = new ImmutableSet.Builder<>();
        }

        private void clear() {
            added.clear();
            removed = new ImmutableSet.Builder<>();
        }

        public void add(final T obj, final C cord) {
            if (obj instanceof Base) {
                addBase((Base) obj, cord);
                return;
            }
            if (cords.containsKey(cord)) {
                throw new RuntimeException(format("Can't add [%s] at [%s] the cord is occupied by [%s]", obj, cord, cords.get(cord)));
            }

            added.add(new Place<>(obj, cord));
            cords.put(cord, obj);
        }

        private void addBase(Base<C> base, C cord) {
            assert base.center.equals(cord);
            assert !cords.keySet().stream().anyMatch(base.area());

            for (var c : base.area) {
                cords.put(c, base);
            }
            added.add(new Place<>((T) base, cord));
            cords.put(cord, base);
        }

        @Override
        public void remove(C cord) {
            removed.add(cord);
        }

        public Set<C> removed() {
            return removed.build();
        }
    }

    private class EntryNoRemove<T> extends Entry<T> {

        @Override
        public void remove(C cord) {
            throw new UnsupportedOperationException("Can't remove");
        }
    }

    class Place<T> {

        private final T obj;
        private final C at;

        Place(T obj, C at) {

            this.obj = obj;
            this.at = at;
        }
    }


}

