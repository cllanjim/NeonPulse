package engine;

import ch.bildspur.postfx.builder.PostFX;
import game.NetworkInput;
import game.Player;
import network.JoinEvent;
import network.NetworkEvent;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.net.Client;
import processing.net.Server;

import java.util.ArrayList;

public abstract class Screen {
    protected PApplet applet;

    protected static ArrayList<Player> players = new ArrayList<>(4);
    protected static ArrayList<Client> clients = new ArrayList<>(4);

    public abstract void load();
    public abstract void update(float deltatime);
    public abstract PGraphics render();
    public abstract void unload();

    public void renderFX(PostFX fx) { }

    // Textures
    protected Screen(PApplet applet) {
        this.applet = applet;
    }

    public void handleInput() { }

    public void handleEvents(Server server) {
        if (server == null) return;
        for (Client current = server.available(); current != null; current = server.available()) {
            byte[] network_event = current.readBytes();
            byte event_code = network_event[0];

            if (network_event.length % 4 != 0) {
                // DEBUG
                PApplet.println("Packet error: ", network_event.length/4 + 1, " :");
                PApplet.println(network_event);
                return;
            }

            switch (event_code) {
                // TODO: Reset input / sync input state
                case NetworkEvent.LEAVE: {
                    int player_index = network_event[1];
                    players.remove(player_index);
                    break;
                }
                case NetworkEvent.INPUT: {
                    int player_index = network_event[1];
                    Player player = players.get(player_index);
                    ((NetworkInput)player.input).network_event = network_event;
                    break;
                }
                case NetworkEvent.JOIN: {
                    int player_index = players.size();
                    PApplet.println("Player ", player_index, " joined");
                    addPlayer(new Player(new NetworkInput(new Input(), current), null));
                    JoinEvent join = new JoinEvent(player_index, true);
                    current.write(join.data);
                    break;
                }
            }
        }
    }

    public static void addClient(Client client) {
        clients.add(client);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
}
