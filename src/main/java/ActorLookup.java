import akka.actor.ActorContext;
import akka.actor.ActorRef;
import com.google.common.collect.Maps;

import java.util.Map;

class ActorLookup {

    private final Map<Integer, ActorRef> sectionRefs;
    private final ActorContext context;


    ActorLookup(ActorContext context) {
        this.sectionRefs = Maps.newHashMap();
        this.context = context;
    }


    ActorRef findSection(Integer sectionNo) {
        var sectionActor = sectionRefs.get(sectionNo);
        if (sectionActor != null) {
            if (sectionActor.isTerminated()) {
                sectionRefs.remove(sectionNo);
                return findSection(sectionNo);
            }
        }

        var path = context.self().path().parent().child(ActorNames.sectionName(sectionNo));

        sectionActor = context.actorFor(path);
        sectionRefs.put(sectionNo, sectionActor);
        return sectionActor;
    }
}
