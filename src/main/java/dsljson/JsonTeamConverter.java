package dsljson;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import world.Team;
import world.Teams;

@JsonConverter(target = Team.class)
public class JsonTeamConverter {


    public static final JsonReader.ReadObject<Team> JSON_READER = reader -> {
        if (reader.wasNull()) return null;
        String s = reader.readSimpleString();
        if (s == "Unassigned") return Teams.None;
        else return Teams.teamForName(s);

    };
    public static final JsonWriter.WriteObject<Team> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
        } else {
            writer.writeString(value.name());
        }

    };

}
