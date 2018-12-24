package dsljson;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.time.Instant;

@JsonConverter(target = Instant.class)
public abstract class JsonInstantConverter {


    public static final JsonReader.ReadObject<Instant> JSON_READER = reader -> {
        if (reader.wasNull()) return null;
        return Instant.parse(reader.readSimpleString());
        //return LocalTime.parse(reader.readSimpleString());
    };
    public static final JsonWriter.WriteObject<Instant> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
        } else {
            //2018-12-23T21:24:41.795
            writer.writeString(value.toString().substring(0, 23));
        }
    };
}

