package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;

import java.time.Instant;


public class CalculationInfo {

    public static CalculationInfo start() {
        return new CalculationInfo();
    }

    public final Instant start;
    public Instant stopped;
    public long elapsed;


    CalculationInfo() {
        this.start = Instant.now();
    }

    //see https://github.com/ngs-doo/dsl-json/blob/master/examples/MavenJava8/src/main/java/com/dslplatform/maven/ImmutablePerson.java
    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    CalculationInfo(Instant start, Instant stopped, long elapsed) {
        this.start = start;
        this.stopped = stopped;
        this.elapsed = elapsed;

    }

    public void stop() {
        this.stopped = Instant.now();
        this.elapsed = stopped.toEpochMilli() - start.toEpochMilli();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", start)
                .add("stopped", stopped)
                .add("elapsed", elapsed)
                .toString();
    }
}
