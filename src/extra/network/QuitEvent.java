package extra.network;

import processing.core.PApplet;

public class QuitEvent extends NetworkEvent {
    public QuitEvent(int player_index, boolean force) {
        data[0] = NetworkEvent.LEAVE;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(force);
    }
}
