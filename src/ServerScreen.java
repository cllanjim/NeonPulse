import engine.Input;
import engine.Screen;
import game.Player;
import network.JoinEvent;
import network.NetworkEvent;
import network.NetworkInput;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.net.Client;
import processing.net.Server;
import processing.sound.SoundFile;

import java.util.ArrayList;

import static processing.core.PApplet.*;

public class ServerScreen extends Screen {
    // Networking
    protected static ArrayList<Client> clients = new ArrayList<>(4);

    private static Server server = null;
    private final SoundFile test_sound;

    ServerScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        test_sound = new SoundFile(applet, "audio/test.wav");
    }

    @Override
    public void load() {
        // Networking

        if (server == null) {
            try {
                server = new Server(applet, NeonPulse.Config.PORT);
            } catch (Exception e) {
                println(e);
            }
        }

        // Load Network players
        for (Client network_client : clients) {
            Player player = new Player(applet, new NetworkInput(new Input(), network_client), test_sound);
            addPlayer(player);
        }
    }

    @Override
    public void handleInput() {
        if (server == null) return;
        for (Client current = server.available(); current != null; current = server.available()) {
            byte[] network_event = current.readBytes();
            byte event_code = network_event[0];

            if (network_event.length % 4 != 0) {
                // DEBUG
                println("Packet error: ", network_event.length / 4 + 1, " :");
                println(network_event);
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
                    ((NetworkInput) player.input).network_event = network_event;
                    break;
                }
                case NetworkEvent.JOIN: {
                    int player_index = players.size();
                    PApplet.println("Player ", player_index, " joined");
                    addPlayer(new Player(applet, new NetworkInput(new Input(), current), null));
                    JoinEvent join = new JoinEvent(player_index, true);
                    current.write(join.data);
                    break;
                }
            }
        }
    }

    @Override
    public void update(float deltatime) {

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
