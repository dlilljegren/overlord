package world;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;

import java.time.Instant;

public class ViewVersion {

    public final Instant time;
    public final int version;
    public final int mainSection;

    private ViewVersion(Instant time, int version, int mainSection) {
        this.time = time;
        this.version = version;
        this.mainSection = mainSection;
    }

    @CompiledJson
    public static ViewVersion create(Instant time, int version, int mainSection) {
        return new ViewVersion(time, version, mainSection);
    }


    public static ViewVersion initial(int sectionNo) {
        return new ViewVersion(Instant.now(), 0, sectionNo);
    }

    public ViewVersion next() {
        return new ViewVersion(Instant.now(), version + 1, mainSection);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mainSection", mainSection)
                .add("version", version)
                .add("time", time)
                .toString();
    }
}
