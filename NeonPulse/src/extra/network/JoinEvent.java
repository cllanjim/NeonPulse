package extra.network;

import processing.core.PApplet;

public class JoinEvent extends NetworkEvent {
    public JoinEvent(int player_index, boolean force) {
        data[0] = NetworkEvent.JOIN;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(force);
    }
}
