package messages.client;

import akka.http.javadsl.model.ws.Message;
import akka.util.ByteString;
import com.dslplatform.json.runtime.Settings;
import messages.ClientCommand;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AkkaWireMessageTest {


    @Test
    public void testParse() throws IOException {
        var dslJson = new com.dslplatform.json.DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

        var bs = ByteString.fromString("a{\"cord\":{\"row\":3,\"col\":4}}");
        Message msg = new akka.http.scaladsl.model.ws.BinaryMessage.Strict(bs);
        AkkaWireMessage wireMessage = new AkkaWireMessage(System.currentTimeMillis(), msg);

        ClientCommand cmd = wireMessage.parse(dslJson);

        System.out.println(cmd);

    }

}