package network;

import processing.core.PApplet;

public class InputEvent extends NetworkEvent {
    public InputEvent(int player_index, int key_index, boolean key_state) {
        data[0] = NetworkEvent.INPUT;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(key_index);
        data[3] = PApplet.parseByte(key_state);
    }
}
