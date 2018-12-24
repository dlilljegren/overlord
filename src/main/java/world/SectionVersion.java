package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;

import java.time.Instant;

public class SectionVersion {

    public final Instant time;
    public final int version;
    public final int section;

    private SectionVersion(Instant time, int version, int section) {
        this.time = time;
        this.version = version;
        this.section = section;
    }

    @CompiledJson
    public static SectionVersion create(Instant time, int version, int section) {
        return new SectionVersion(time, version, section);
    }


    public static SectionVersion initial(int sectionNo) {
        return new SectionVersion(Instant.now(), 0, sectionNo);
    }

    SectionVersion next() {
        return new SectionVersion(Instant.now(), version + 1, section);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mainSection", section)
                .add("version", version)
                .add("time", time)
                .toString();
    }
}
