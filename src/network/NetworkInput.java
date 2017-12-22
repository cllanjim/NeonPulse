package network;

import engine.Input;
import game.KeyboardInput;
import game.Player;
import processing.core.PApplet;
import processing.net.Client;

public class NetworkInput extends KeyboardInput {
    private Client client;
    public byte[] network_event;

    public NetworkInput(Input input_manager, Client client) {
        super(input_manager);
        this.client = client;
    }

    public void handleInput(Player player) {
        if (network_event != null) {
            for (int current_byte = 0; current_byte < network_event.length; current_byte += 4) {
                try {
                    int key_index = network_event[current_byte + 2];
                    boolean key_state = network_event[current_byte + 3] != 0;
                    input.setKeyState(key_index, key_state);
                } catch (Exception e) {
                    PApplet.println(e);
                }
            }
        }
        super.handleInput(player);
    }
}