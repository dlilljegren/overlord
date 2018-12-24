package games;

import messages.ClientCommand;

import java.io.InputStream;

public interface IJson {

    String toJson(Object o);

    <T> T toObject(Class<T> model, String s);

    <T extends ClientCommand> T deserialize(Class<T> klass, InputStream inputStream);
}
