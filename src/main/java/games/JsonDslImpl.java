package games;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;
import messages.ClientCommand;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonDslImpl implements IJson {
    private final DslJson<Object> dslJson;
    private final ThreadLocal<JsonWriter> threadWriter =
            new ThreadLocal<JsonWriter>() {
                @Override
                protected JsonWriter initialValue() {
                    return dslJson.newWriter(8096 * 4);
                }
            };

    JsonDslImpl() {


        dslJson = new com.dslplatform.json.DslJson<>(
                Settings
                        .withRuntime()
                        .allowArrayFormat(true)
                        .includeServiceLoader()
                        .doublePrecision(JsonReader.DoublePrecision.LOW));

    }

    private JsonWriter writer() {
        return threadWriter.get();
    }

    @Override
    public String toJson(Object o) {
        JsonWriter writer = writer();
        writer.reset();
        writer.serializeObject(o);
        return writer.toString();
    }

    @Override
    public <T> T toObject(Class<T> model, String s) {
        try {
            return dslJson.deserialize(model, new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends ClientCommand> T deserialize(Class<T> klass, InputStream inputStream) {
        try {
            return dslJson.deserialize(klass, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
