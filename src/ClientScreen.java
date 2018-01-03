import engine.Input;
import engine.InputHandler;
import game.KeyboardInput;
import game.Player;
import network.*;
import processing.core.PApplet;
import processing.net.Client;

import java.util.ArrayList;

import static processing.core.PApplet.println;

public class ClientScreen extends TestScreen implements InputHandler {
    private final ArrayList<NetworkEvent> networkEvents = new ArrayList<>(8);
    private Client gameClient;

    private int playerIndex = 0;
    private boolean active = false;

    ClientScreen(PApplet applet) {
        super(applet);
    }

    @Override
    public void loadPlayers() {
        if (gameClient == null) {
            try {
                gameClient = new Client(applet, "127.0.0.1", NeonPulse.Config.PORT);
            } catch (Exception e) {
                println(e);
            }
        }

        if (gameClient != null && gameClient.active()) {
            JoinEvent join = new JoinEvent(playerIndex, true);
            gameClient.write(join.data);
        } else {
            System.exit(-1);
        }

        players.clear();

        if (NeonPulse.Config.KEYBOARD) {
            testPlayer = new Player(applet, new KeyboardInput(NeonPulse.g_input), NeonPulse.Debug.test_sound);
            addPlayer(testPlayer);
        }
    }

    @Override
    public void update(float deltatime) {
        readNetworkEvents(gameClient);

        if (active) {
            sendNetworkEvents(gameClient);
        }

        networkEvents.clear();

        super.update(deltatime);
    }

    private void sendNetworkEvents(Client g_game_client) {
        for (NetworkEvent evt : networkEvents) {
            g_game_client.write(evt.data);
        }
    }

    private void readNetworkEvents(Client g_game_client) {
        if (g_game_client != null && g_game_client.available() > 0) {
            // Read in the bytes
            byte[] network_event = g_game_client.readBytes();
            if (network_event != null) {
                byte event_code = network_event[0];
                switch (event_code) {
                    case NetworkEvent.JOIN: {
                        playerIndex = network_event[1];
                        active = true;
                        break;
                    }
                    case NetworkEvent.LEAVE: {
                        playerIndex = 0;
                        active = false;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onKeyPressed(int key, int keyCode) {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            networkEvents.add(new KeyEvent(playerIndex, key_index, true));
        }
    }

    @Override
    public void onKeyReleased(int key, int keyCode) {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            networkEvents.add(new KeyEvent(playerIndex, key_index, false));
        }
    }

    @Override
    public void onButtonPressed(int button) {
        int button_index = Input.getButtonIndex(button);
        if (button_index >= 0) {
            networkEvents.add(new ButtonEvent(playerIndex, button_index, false));
        }
    }

    @Override
    public void onButtonReleased(int button) {
        int button_index = Input.getButtonIndex(button);
        if (button_index >= 0) {
            networkEvents.add(new ButtonEvent(playerIndex, button_index, false));
        }
    }

    @Override
    public void unload() {
        NeonPulse.g_input.removeListener(this);
        if (gameClient != null && gameClient.active()) {
            QuitEvent quit = new QuitEvent(playerIndex, true);
            gameClient.write(quit.data);
        }
    }
}
