package world;

import com.dslplatform.json.CompiledJson;

import java.util.Objects;

@CompiledJson
public class ColorTeam implements Team {

    private final String color;


    public ColorTeam(String name) {
        this.color = name;
    }


    @Override
    public String name() {
        return this.color;
    }

    public String toString() {
        return String.format("Team-%s", name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorTeam colorTeam = (ColorTeam) o;
        return Objects.equals(color, colorTeam.color);
    }

    @Override
    public int hashCode() {

        return Objects.hash(color);
    }
}
