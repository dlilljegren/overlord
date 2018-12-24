package world;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StreamUtils {
    public static <K, V> Map<K, V> filterByKey(Map<K, V> map, Predicate<K> predicate) {
        return map.entrySet()
                .stream()
                .filter(x -> predicate.test(x.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
