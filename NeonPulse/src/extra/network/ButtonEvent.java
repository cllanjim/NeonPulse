package extra.network;

import processing.core.PApplet;

public class ButtonEvent extends NetworkEvent {
    public ButtonEvent(int player_index, int button_index, boolean button_state) {
        data[0] = NetworkEvent.BUTTON;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(button_index);
        data[3] = PApplet.parseByte(button_state);
    }
}
