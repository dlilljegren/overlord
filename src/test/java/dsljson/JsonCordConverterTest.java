package dsljson;

import com.dslplatform.json.DslJson;
import games.GameDefinition;
import games.GameDefinitions;
import org.junit.jupiter.api.Test;
import world.Cord;
import world.View;
import world.ViewDefinition;
import world.WorldCord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonCordConverterTest {

    @Test
    public void testConverter() throws IOException {
        DslJson<Object> dslJson = new DslJson<>();
        var c = Cord.at(0, 2);
        var bytes = new byte[100];
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        GameDefinition gd = GameDefinitions.SMALL;

        //Set the view at 0,0 in the mainSection cord system, then they are the same
        ViewDefinition vd = new ViewDefinition(WorldCord.at(10, 10), 0, 30, 30);
        View view = new View(gd.worldDefinition(), vd);
        JsonCordConverter.setMapping(view.sectionToView(0), view.viewToSection(0));

        System.out.println(view.sectionToView(0).apply(c));

        dslJson.serialize(c, b);

        assertEquals("{\"col\":0,\"row\":2}", b.toString(Charset.defaultCharset()));
    }

}