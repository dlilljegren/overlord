package messages.client;

import akka.http.javadsl.model.ws.Message;
import akka.util.ByteString;
import com.dslplatform.json.DslJson;
import com.google.common.collect.ImmutableMap;
import games.IJson;
import messages.ClientCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Message sent by the web client
 */
public class AkkaWireMessage {

    private static final Logger L = LogManager.getLogger(AkkaWireMessage.class);

    private static Charset UTF8 = Charset.forName("UTF-8");

    private final long serverReceivedTime;
    private final Message webSocketMessage;

    private final static Map<Byte, Class<? extends ClientCommand>> commands = new ImmutableMap.Builder<Byte, Class<? extends ClientCommand>>()
            .put(Byte.valueOf((byte) 'a'), ClientCommand.AddUnit.class)
            .put(Byte.valueOf((byte) 'r'), ClientCommand.RemoveUnit.class)
            .put(Byte.valueOf((byte) 'm'), ClientCommand.MoveCursor.class)
            .put(Byte.valueOf((byte) 'l'), ClientCommand.Login.class)
            .put(Byte.valueOf((byte) 's'), ClientCommand.GoToSection.class)
            .build();

    public AkkaWireMessage(long serverReceivedTime, Message webSocketMessage) {
        this.serverReceivedTime = serverReceivedTime;
        this.webSocketMessage = webSocketMessage;

    }

    public AkkaWireMessage(Message webSocketMessage) {
        this(System.currentTimeMillis(), webSocketMessage);
    }


    public ClientCommand parse(DslJson<Object> dslJson) throws IOException {
        ByteString byteStr = this.webSocketMessage.asBinaryMessage().getStrictData();
        ByteBuffer buffer = byteStr.asByteBuffer();


        byte header = buffer.get();
        Class<? extends ClientCommand> klass = getClass(header);
        var parsed = dslJson.deserialize(klass, new ByteBufferBackedInputStream(buffer));
        L.info("Parsed binary msg into [{}]", parsed);
        return parsed;
    }

    public ClientCommand parse(IJson json) {
        ByteBuffer buffer = buffer();

        byte header = buffer.get();
        Class<? extends ClientCommand> klass = getClass(header);
        var parsed = json.deserialize(klass, new ByteBufferBackedInputStream(buffer));

        L.info("Parsed binary msg into [{}]", parsed);
        return parsed;

    }

    private ByteBuffer buffer() {
        if (!this.webSocketMessage.isText()) {
            ByteString byteStr = this.webSocketMessage.asBinaryMessage().getStrictData();
            ByteBuffer buffer = byteStr.asByteBuffer();
            return buffer;
        } else {
            String txt = this.webSocketMessage.asTextMessage().getStrictText();
            return ByteBuffer.wrap(txt.getBytes(UTF8));
        }
    }


    private Class<? extends ClientCommand> getClass(byte header) {
        final var c = commands.get(header);
        assert c != null : "No class matching header " + header;
        return c;
    }

    public class ByteBufferBackedInputStream extends InputStream {

        ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        public int read() throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }

        public int read(byte[] bytes, int off, int len)
                throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }

            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }
}
