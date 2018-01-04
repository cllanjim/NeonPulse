import engine.Input;
import game.KeyboardInput;
import network.JoinEvent;
import network.NetworkEvent;
import network.NetworkPlayer;
import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

import java.util.ArrayList;

import static processing.core.PApplet.println;

public class ServerScreen extends TestScreen {
    private final ArrayList<NetworkPlayer> networkPlayers;

    private static Server server = null;

    ServerScreen(PApplet applet) {
        super(applet);
        networkPlayers = new ArrayList<>();
    }

    @Override
    public void loadPlayers() {
        if (server == null) {
            try {
                server = new Server(applet, NeonPulse.Config.PORT);
            } catch (Exception e) {
                println(e);
            }
        }

        players.clear();

        if (server != null && server.active()) {
            for (int i = 0; i < server.clientCount; i++) {
                Client client = server.clients[i];
                NetworkPlayer player = new NetworkPlayer(applet, new KeyboardInput(new Input()), client, NeonPulse.Debug.test_sound);
                networkPlayers.add(player);
                addPlayer(player);
            }
        }
    }

    @Override
    public void handleInput() {
        if (server == null) return;
        for (Client current = server.available(); current != null; current = server.available()) {
            byte[] network_event = current.readBytes();

            if (network_event.length % 4 != 0 || network_event.length == 0) {
                // DEBUG
                println("Packet error: ", network_event.length / 4 + 1, " :");
                println(network_event);
                return;
            }

            for (int current_byte = 0; current_byte < network_event.length; current_byte += 4) {
                try {
                    byte event_code = network_event[0];
                    switch (event_code) {
                        // TODO: Reset input / sync input state
                        case NetworkEvent.LEAVE: {
                            int player_index = network_event[1];
                            networkPlayers.remove(player_index);
                            break;
                        }
                        case NetworkEvent.KEY: {
                            int player_index = network_event[1];
                            NetworkPlayer player = networkPlayers.get(player_index);
                            player.handleNetworkEvent(network_event);
                            break;
                        }
                        case NetworkEvent.JOIN: {
                            int player_index = players.size();
                            PApplet.println("Player ", player_index, " joined");
                            addPlayer(new NetworkPlayer(applet, new KeyboardInput(new Input()), current, null));
                            JoinEvent join = new JoinEvent(player_index, true);
                            current.write(join.data);
                            break;
                        }
                    }
                } catch (Exception e) {
                    PApplet.println(e);
                }
            }
        }
    }
}
