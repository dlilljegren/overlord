package messages;

import java.util.UUID;

public abstract class SessionCommand {
    public final UUID sessionId;


    protected SessionCommand(UUID sessionId) {
        this.sessionId = sessionId;
    }


}
