package world;

import changes.SectionUpdateBuilder;
import changes.ViewUpdateBuilder;
import com.dslplatform.json.runtime.Settings;
import games.GameDefinitions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;

class JsonUpdateTest {

    @Test
    public void sectionToJson() throws IOException {

        var cb = new SectionUpdateBuilder();
        cb.add(Cord.at(0, 0), Unit.forTeam("red"));
        var stateObj = cb.buildSnapshot(SectionVersion.create(Instant.now(), 1, 0));

        var dslJson = new com.dslplatform.json.DslJson<>(
                Settings.basicSetup()
        );


        var byteStream = new ByteArrayOutputStream();


        dslJson.serialize(stateObj, byteStream);
        System.out.println(byteStream.toString(Charset.defaultCharset()));

        //String actual = com.google.common.io.Resources.getResource("sections/simple.json").getJsonContent().toJson();
        //var state =GameDefinitions.SMALL.serializer().toObject(StateChange.class,actual);


    }


    @Test
    public void viewToJson() throws IOException {
        var game = GameDefinitions.SMALL;
        var view = game.worldDefinition().createViewAtUpperLeft(0, 10, 10);

        var cb = new ViewUpdateBuilder(view);
        cb.add(WorldCord.at(0, 0), Unit.forTeam("red"));
        cb.add(WorldCord.at(1, 1), Terrain.Hill);
        var stateObj = cb.buildSnapshot(ViewVersion.initial(1));

        var dslJson = new com.dslplatform.json.DslJson<>(
                Settings.basicSetup()
        );


        var byteStream = new ByteArrayOutputStream();
        dslJson.serialize(stateObj, byteStream);
        System.out.println(byteStream.toString(Charset.defaultCharset()));

        //String actual = com.google.common.io.Resources.getResource("sections/simple.json").getJsonContent().toJson();
        //var state =GameDefinitions.SMALL.serializer().toObject(StateChange.class,actual);


    }


}