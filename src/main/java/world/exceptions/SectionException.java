package world.exceptions;

public abstract class SectionException extends RuntimeException {

    public final int sectionNo;

    SectionException(int sectionNo, String message) {
        super(message);
        this.sectionNo = sectionNo;
    }
}
