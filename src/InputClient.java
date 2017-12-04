import network.InputEvent;
import network.JoinEvent;
import network.NetworkEvent;
import engine.Input;
import processing.core.*;
import processing.net.*;

import java.util.ArrayList;

public class InputClient extends PApplet {
    private Client g_game_client;

    private ArrayList<NetworkEvent> g_events = new ArrayList<>(8);

    private int g_player_index = 0;
    private boolean g_active = false;

    public void setup() {
        g_game_client = new Client(this, "127.0.0.1", NeonPulse.Config.PORT);
        if (g_game_client.active()) {
            JoinEvent join = new JoinEvent( g_player_index, true);
            g_game_client.write(join.data);
        } else {
            System.exit(-1);
        }
    }

    public void draw() {
        background(key);
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
                    case NetworkEvent.LEAVE: {
                        g_player_index = 0;
                        g_active = false;
                        break;
                    }
                    case NetworkEvent.JOIN: {
                        g_player_index = network_event[1];
                        g_active = true;
                        break;
                    }
                }
            }
        }
    }

    public void keyPressed() {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            g_events.add(new InputEvent(g_player_index, key_index, true));
        }
    }

    public void keyReleased() {
        int key_index = Input.getKeyIndex(key);
        if (key_index >= 0) {
            g_events.add(new InputEvent(g_player_index,key_index, false));
        }
    }

    public void settings() {
        size(200, 200);
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"InputClient"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
