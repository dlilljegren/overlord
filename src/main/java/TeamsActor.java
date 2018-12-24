import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import games.GameDefinition;
import messages.TeamsMessage;
import world.TeamManager;

import java.util.stream.Collectors;

import static java.lang.String.format;

public class TeamsActor extends AbstractActor {
    static String ActorName = "Teams";

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final TeamManager manager;

    private TeamsActor(GameDefinition gameDefinition) {
        this.manager = new TeamManager(gameDefinition.availableTeams().collect(Collectors.toList()));
    }

    public static Props props(GameDefinition gameDefinition) {
        return Props.create(TeamsActor.class, gameDefinition);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TeamsMessage.RequestRecommended.class, this::process)//Other players are moving the cursor in this players sector
                .match(TeamsMessage.ActivatePlayer.class, this::process)
                .match(TeamsMessage.DeactivatePlayer.class, this::process)
                //.match(DistributedPubSubMediator.SubscribeAck.class, this::process)
                //.match(DistributedPubSubMediator.UnsubscribeAck.class, this::process)
                .matchAny(o -> log.info("received unknown message [{}] when in state playing", o))
                .build();
    }

    private void process(TeamsMessage.DeactivatePlayer deactivatePlayer) {
        var wasFound = manager.deactivatePlayer(deactivatePlayer.playerInfo.player);
        assert wasFound : format("Player [%s] was deactivated but not found in existing list", deactivatePlayer.playerInfo.player);
        log.debug("Player [{}] deactivated", deactivatePlayer.playerInfo.player);
    }

    private void process(TeamsMessage.ActivatePlayer activatePlayer) {
        var wasNew = manager.activatePlayer(activatePlayer.playerInfo.player);
        assert wasNew : format("Player [%s] was activated but was already in list of active players", activatePlayer.playerInfo.player);
        log.debug("Player [{}] activated", activatePlayer.playerInfo.player);
    }

    private void process(TeamsMessage.RequestRecommended requestRecommended) {
        var recommended = manager.recommendedTeam();
        log.debug("Recommend team [{}]", recommended);
        sender().tell(new TeamsMessage.RecommendedReply(recommended), self());
    }
}
