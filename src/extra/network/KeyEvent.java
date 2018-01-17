package extra.network;

import processing.core.PApplet;

public class KeyEvent extends NetworkEvent {
    public KeyEvent(int player_index, int key_index, boolean key_state) {
        data[0] = NetworkEvent.KEY;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(key_index);
        data[3] = PApplet.parseByte(key_state);
    }
}
