package messages;

import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonWriter;

public class DataMessage implements JsonObject {
    public final String message;
    public final Object data;


    public DataMessage(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    //@see https://github.com/ngs-doo/dsl-json/blob/master/examples/MavenJava8/src/main/java/com/dslplatform/maven/Example.java
    @Override
    public void serialize(JsonWriter writer, boolean b) {
        writer.writeAscii("{\"message\":");
        writer.writeString(message);

        writer.writeAscii(",\"data\":");
        writer.serializeObject(data);

        writer.writeAscii("}");

    }


}
