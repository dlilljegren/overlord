package util;

import java.util.concurrent.atomic.AtomicInteger;

public class Namer {

    private final String baseName;

    private final AtomicInteger n = new AtomicInteger(1);

    public Namer(String baseName) {
        this.baseName = baseName;
    }

    String next() {
        return String.format("%s-%s", baseName, n.getAndAdd(1));
    }
}
