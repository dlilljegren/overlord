package dsljson;

import com.dslplatform.json.*;
import world.Base;
import world.ICord;

@JsonConverter(target = Base.class)
public class BaseConverter {
    private static final java.nio.charset.Charset utf8 = java.nio.charset.Charset.forName("UTF-8");
    private static final byte[] name = "\"name\":".getBytes(utf8);
    private static final byte[] radius = ",\"radius\":".getBytes(utf8);
    private static final byte[] center = ",\"center\":".getBytes(utf8);
    private static final byte[] zoc = ",\"zoc\":".getBytes(utf8);
    private static final byte[] area = ",\"area\":".getBytes(utf8);

    private static final byte[] col = "\"col\":".getBytes(utf8);
    private static final byte[] row = ",\"row\":".getBytes(utf8);

    public final static JsonWriter.WriteObject<Base> JSON_WRITER = new JsonWriter.WriteObject<>() {
        public void write(JsonWriter writer, Base value) {
            writer.writeAscii("{");

            writer.writeAscii(name);
            StringConverter.serialize(value.name, writer);

            writer.writeAscii(radius);
            NumberConverter.serialize(value.radius, writer);

            writer.writeAscii(center);
            writer.serializeObject(value.center);

            writer.writeAscii(zoc);
            writer.serialize(value.zoneOfControl, CORD_WRITER);

            writer.writeAscii(area);
            writer.serialize(value.area, CORD_WRITER);

            writer.writeAscii("}");

        }
    };

    private final static JsonWriter.WriteObject<ICord> CORD_WRITER = (writer, c) -> {
        writer.writeAscii("{");
        writer.writeAscii(col);
        NumberConverter.serialize(c.col(), writer);

        writer.writeAscii(row);
        NumberConverter.serialize(c.row(), writer);

        writer.writeAscii("}");
    };

    public final static JsonReader.ReadObject<Base> JSON_READER = reader -> {
        throw new UnsupportedOperationException();
    };
}
