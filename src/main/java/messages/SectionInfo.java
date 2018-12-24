package messages;

import akka.actor.ActorRef;
import com.google.common.base.MoreObjects;

public class SectionInfo {
    public final ActorRef sectionActor;
    public final Integer sectionNo;


    public SectionInfo(ActorRef sectorActor, Integer sectorNo) {
        this.sectionActor = sectorActor;
        this.sectionNo = sectorNo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sectionNo", sectionNo)
                .add("sectionActor", sectionActor)
                .toString();
    }
}
