import engine.Input;
import engine.Screen;
import network.InputEvent;
import network.JoinEvent;
import network.NetworkEvent;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.net.Client;

import java.util.ArrayList;

public class ClientScreen extends Screen {
    private Client g_game_client;
    private final ArrayList<NetworkEvent> g_events = new ArrayList<>(8);

    private int g_player_index = 0;
    private boolean g_active = false;

    ClientScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, PConstants.P2D);

    }

    @Override
    public void load() {
        g_game_client = new Client(applet, "127.0.0.1", NeonPulse.Config.PORT);
        if (g_game_client.active()) {
            JoinEvent join = new JoinEvent( g_player_index, true);
            g_game_client.write(join.data);
        } else {
            System.exit(-1);
        }
    }

    @Override
    public void update(float deltatime) {
        readNetworkEvents(g_game_client);

        if (g_active) {
            sendNetworkEvents(g_game_client);
        }

        g_events.clear();
    }

    private void sendNetworkEvents(Client g_game_client) {
        for (NetworkEvent evt : g_events) {
            g_game_client.write(evt.data);
        }
    }

    private void readNetworkEvents(Client g_game_client) {
        if (g_game_client.available() > 0) {
            // Read in the bytes
            byte[] network_event = g_game_client.readBytes();
            if ( network_event != null ) {
                byte event_code = network_event[0];
                switch (event_code) {
                    case NetworkEvent.JOIN: {
                        g_player_index = network_event[1];
                        g_active = true;
                        break;
                    }
                    case NetworkEvent.LEAVE: {
                        g_player_index = 0;
                        g_active = false;
                        break;
                    }
                }
            }
        }
    }

    public void onKeyPressed(int key) {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            g_events.add(new InputEvent(g_player_index, key_index, true));
        }
    }

    public void onKeyReleased(int key) {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            g_events.add(new InputEvent(g_player_index,key_index, false));
        }
    }

    @Override
    public PGraphics render() {
        canvas.beginDraw();
        canvas.endDraw();
        return canvas;
    }

    @Override
    public void unload() {

    }
}
