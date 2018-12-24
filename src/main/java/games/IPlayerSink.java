package games;

import messages.DataMessage;

public interface IPlayerSink {

    void sendMessage(DataMessage msg);
}
