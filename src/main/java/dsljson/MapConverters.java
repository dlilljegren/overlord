package dsljson;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import world.*;

import java.io.IOException;
import java.util.Map;

public abstract class MapConverters<C extends ICord, T> {


    protected MapConverters() {

    }

    final JsonReader.ReadObject<Map<C, T>> jsonReader = new JsonReader.ReadObject<>() {
        public Map<C, T> read(JsonReader reader) throws IOException {
            reader.getNextToken();
            //return NumberConverter.deserializeIntNullableCollection(reader);
            throw new UnsupportedOperationException("Not implemented");
        }
    };

    final JsonWriter.WriteObject<Map.Entry<C, T>> entryWriter = new JsonWriter.WriteObject<>() {
        public void write(JsonWriter writer, Map.Entry<C, T> value) {
            writer.writeAscii("{\"at\":");
            writeCord(value.getKey(), writer);
            writer.writeAscii(",");
            writer.writeAscii("\"e\":");
            writer.serializeObject(value.getValue());
            writer.writeAscii("}");
        }
    };

    final JsonWriter.WriteObject<Map<C, T>> jsonWriter = new JsonWriter.WriteObject<>() {
        public void write(JsonWriter writer, Map<C, T> value) {
            writer.serialize(value.entrySet(), entryWriter);
        }
    };


    private static void writeCord(ICord c, JsonWriter writer) {
        writer.writeAscii("{\"col\":");
        NumberConverter.serialize(c.col(), writer);
        writer.writeAscii(",");
        writer.writeAscii("\"row\":");
        NumberConverter.serialize(c.row(), writer);
        writer.writeAscii("}");
    }

    public static class WorldTerrainConverter extends MapConverters<WorldCord, Terrain> {
        private WorldTerrainConverter() {

        }

        static final WorldTerrainConverter $ = new WorldTerrainConverter();

        public final static JsonReader.ReadObject<Map<WorldCord, Terrain>> JSON_READER = $.jsonReader;
        public final static JsonWriter.WriteObject<Map<WorldCord, Terrain>> JSON_WRITER = $.jsonWriter;
    }

    public static class WorldBaseConverter extends MapConverters<WorldCord, Base<WorldCord>> {
        private WorldBaseConverter() {

        }

        static final WorldBaseConverter $ = new WorldBaseConverter();

        public final static JsonReader.ReadObject<Map<WorldCord, Base<WorldCord>>> JSON_READER = $.jsonReader;
        public final static JsonWriter.WriteObject<Map<WorldCord, Base<WorldCord>>> JSON_WRITER = $.jsonWriter;
    }

    public static class WorldUnitConverter extends MapConverters<WorldCord, Unit> {
        private WorldUnitConverter() {

        }

        static final WorldUnitConverter $ = new WorldUnitConverter();

        public final static JsonReader.ReadObject<Map<WorldCord, Unit>> JSON_READER = $.jsonReader;
        public final static JsonWriter.WriteObject<Map<WorldCord, Unit>> JSON_WRITER = $.jsonWriter;
    }
}
