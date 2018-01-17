package extra.network;

import engine.InputState;
import game.Player;
import game.PlayerInput;
import processing.core.PApplet;
import processing.net.Client;
import processing.sound.SoundFile;

public class NetworkPlayer extends Player {
    private final Client client;
    private InputState inputState;

    public NetworkPlayer(PApplet applet, PlayerInput playerInput, Client client, SoundFile player_sound) {
        super(applet, playerInput, player_sound);
        this.client = client;
    }

    public void handleNetworkEvent(byte[] network_event) {
        int key_index = network_event[2];
        boolean key_state = network_event[3] != 0;
        inputState.setKeyState(key_index, key_state);
    }
}
