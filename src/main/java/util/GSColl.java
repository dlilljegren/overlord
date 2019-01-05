package util;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.function.Supplier;
import java.util.stream.Collector;

public class GSColl {


    public static <T> Collector<? super T, ?, ImmutableSet<T>> immutableSet() {
        Supplier<UnifiedSet<T>> sp = UnifiedSet::new;

        return Collector.of(sp,
                UnifiedSet::add, (s1, s2) -> s1.withAll(s2), s -> s.toImmutable(), Collector.Characteristics.UNORDERED);
    }
}
