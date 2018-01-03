package network;

import processing.core.PApplet;

public class MouseEvent extends NetworkEvent {
    public MouseEvent(int player_index, int x, int y) {
        data[0] = NetworkEvent.MOUSE;
        data[1] = PApplet.parseByte(player_index);
        data[2] = PApplet.parseByte(x);
        data[3] = PApplet.parseByte(y);
    }
}
