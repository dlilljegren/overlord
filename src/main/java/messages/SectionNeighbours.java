package messages;

import akka.actor.ActorRef;

import java.util.Map;

public class SectionNeighbours {

    public final Map<Integer, ActorRef> masters;
    public final Map<Integer, ActorRef> slaves;
    public final Map<Integer, ActorRef> normals;


    public SectionNeighbours(Map<Integer, ActorRef> masters, Map<Integer, ActorRef> slaves, Map<Integer, ActorRef> normals) {
        this.masters = masters;
        this.slaves = slaves;
        this.normals = normals;
    }
}
