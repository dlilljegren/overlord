package world;

import static java.lang.String.format;


public interface Team {


    String name();


    default Player createPlayer(String name) {
        return new Player(name, this);
    }

    default Player createPlayer(int no) {
        return new Player(format("%s-%d", this.name(), no), this);
    }
}
