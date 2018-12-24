package world;

public interface ICordAware extends ICord {

    ICord cord();

    default int col() {
        return cord().col();
    }

    default int row() {
        return cord().row();
    }
}
