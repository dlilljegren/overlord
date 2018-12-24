package dsljson;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import world.Cord;
import world.WorldCord;

import java.util.function.Function;

@Deprecated
public abstract class JsonCordConverter {

    public static class Mapper {
        private final Function<Cord, WorldCord> sectionToView;
        private final Function<WorldCord, Cord> viewToSection;

        public Mapper(Function<Cord, WorldCord> sectionToView, Function<WorldCord, Cord> viewToSection) {
            this.sectionToView = sectionToView;
            this.viewToSection = viewToSection;
        }
    }

    final static ThreadLocal<Mapper> threadLocal = new ThreadLocal<>();

    public static void setMapping(Function<Cord, WorldCord> sectionToView, Function<WorldCord, Cord> viewToSection) {
        threadLocal.set(new Mapper(sectionToView, viewToSection));
    }


    public static final JsonReader.ReadObject<Cord> JSON_READER = reader -> {
        return read(reader);
    };
    public static final JsonWriter.WriteObject<Cord> JSON_WRITER = (writer, value) -> {
        if (value == null) {
            writer.writeNull();
        } else {
            var wc = threadLocal.get().sectionToView.apply(value);
            writer.writeByte((byte) '{');
            writer.writeByte((byte) '"');
            writer.writeAscii("col");
            writer.writeByte((byte) '"');
            writer.writeByte((byte) ':');
            com.dslplatform.json.NumberConverter.serialize(wc.col, writer);
            writer.writeByte((byte) ',');
            writer.writeByte((byte) '"');
            writer.writeAscii("row");
            writer.writeByte((byte) '"');
            writer.writeByte((byte) ':');
            com.dslplatform.json.NumberConverter.serialize(wc.row, writer);
            writer.writeByte((byte) '}');
        }
    };


    public static world.Cord read(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
        if (reader.wasNull()) return null;
        else if (reader.last() != '{')
            throw new java.io.IOException("Expecting '{' " + reader.positionDescription() + ". Found " + (char) reader.last());
        reader.getNextToken();
        return readContent(reader);
    }

    public static world.Cord readContent(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
        int _col_ = 0;
        int _row_ = 0;
        if (reader.last() == '}') {
            return world.Cord.at(_col_, _row_);
        }
        switch (reader.fillName()) {
            case -225586063:
                reader.getNextToken();
                _col_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
                reader.getNextToken();
                break;
            case 1141775739:
                reader.getNextToken();
                _row_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
                reader.getNextToken();
                break;
            default:
                reader.getNextToken();
                reader.skip();
        }
        while (reader.last() == ',') {
            reader.getNextToken();
            switch (reader.fillName()) {
                case -225586063:
                    reader.getNextToken();
                    _col_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
                    reader.getNextToken();
                    break;
                case 1141775739:
                    reader.getNextToken();
                    _row_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
                    reader.getNextToken();
                    break;
                default:
                    reader.getNextToken();
                    reader.skip();
            }
        }
        if (reader.last() != '}')
            throw new java.io.IOException("Expecting '}' " + reader.positionDescription() + ". Found " + (char) reader.last());
        return world.Cord.at(_col_, _row_);
    }
}
