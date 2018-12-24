package changes;

import world.ICord;

public interface IEntry<T, C extends ICord> {

    void add(T obj, C cord);

    void remove(C cord);
}
