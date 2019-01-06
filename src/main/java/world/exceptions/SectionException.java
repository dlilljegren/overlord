package world.exceptions;

public class SectionException extends RuntimeException {

    public final int sectionNo;

    public SectionException(int sectionNo, String message) {
        super(message);
        this.sectionNo = sectionNo;
    }
}
