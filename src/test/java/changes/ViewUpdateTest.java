package changes;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;
import games.GameDefinitions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.Unit;
import world.View;
import world.ViewVersion;
import world.WorldCord;

import java.io.IOException;

class ViewUpdateTest {
    static DslJson<Object> dslJson;
    static Unit unit;

    @BeforeAll
    static void setUp() {
        dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
        unit = Unit.forTeam("red");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void stateChangeSerialization() throws IOException {
        var game = GameDefinitions.SMALL;
        var cb = new ViewUpdateBuilder(View.create(game.worldDefinition(), 0, 10, 10));
        cb.add(WorldCord.at(5, 5), unit);
        var underTest = cb.buildSnapshot(ViewVersion.initial(1));
        JsonWriter writer = dslJson.newWriter(8096);
        writer.serializeObject(underTest);
        System.out.println(writer.toString());

        ViewUpdate back = dslJson.deserialize(ViewUpdate.class, writer.getByteBuffer(), writer.size());
    }

    @Test
    public void testJsonPojo() throws IOException {
        Pojo pojo = new Pojo(42, "Hubba");
        JsonWriter writer = dslJson.newWriter(8096);
        writer.serializeObject(pojo);
        System.out.println(writer.toString());

        pojo = dslJson.deserialize(Pojo.class, writer.getByteBuffer(), writer.size());
        System.out.println(pojo.x);
    }

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    static class Pojo {
        public final int x;
        public final String s;


        Pojo(int x, String s) {
            this.x = x;
            this.s = s;
        }
    }
}