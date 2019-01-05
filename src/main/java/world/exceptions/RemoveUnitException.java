package world.exceptions;

import world.Cord;

public class RemoveUnitException extends SectionException {
    public final Cord cord;

    protected RemoveUnitException(int sectionNo, Cord cord, String message) {
        super(sectionNo, message);
        this.cord = cord;
    }
}
