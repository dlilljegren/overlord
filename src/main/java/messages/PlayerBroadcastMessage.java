package messages;

import world.Player;

/**
 * Messages braodcasted by the Player Actor
 */
public abstract class PlayerBroadcastMessage {
    public final Player player;


    protected PlayerBroadcastMessage(Player player) {
        this.player = player;
    }

    public static class PlayerState extends PlayerBroadcastMessage {
        public final int availableUnits;

        public PlayerState(Player player, int availableUnits) {
            super(player);
            this.availableUnits = availableUnits;
        }
    }
}
