import game.*;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.net.Client;
import processing.sound.SoundFile;
import engine.Input;
import engine.Screen;
import engine.Tilemap;

import java.util.List;

import static processing.core.PConstants.P2D;

public class TiledScreen extends Screen {
    private Tilemap tilemap;
    private PGraphics canvas;
    private int current_map_index = 0;
    private SoundFile test_sound;

    private static final String[] MAPS = {
            "map1.tmx",
            "map2.tmx",
            "map3.tmx",
    };

    TiledScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        test_sound = new SoundFile(applet,"audio/test.wav");
    }

    public void load() {
        players.clear();

        tilemap = new Tilemap(applet, MAPS[current_map_index]);

        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(new KeyboardInput(NeonPulse.g_input), test_sound);
            addPlayer(player);
        }

        // Load Network players
        for (Client network_client : clients) {
            addPlayer(new Player(new NetworkInput(new Input(), network_client), test_sound));
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.g_control_io.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.g_controller_configs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(new GamepadInput(gamepad), test_sound);
                    addPlayer(player);
                    break;
                }
            }
        }
    }

    public void update(float delta_time) {
        tilemap.map.update(delta_time * 1000);
        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);
            player.update(players, delta_time);
            player.updateMovement(delta_time);

            // Collide current player with others, starting with the one after it
            for (int j = i + 1; j < players.size(); j++) {
                Player other = players.get(j);
                player.collideWithAgent(other);
            }

            // Collide with every player's effects.
            for (int j = 0; j < players.size(); j++ ) {
                // Don't collide with own stuff.
                if (i == j) continue;
                Player other = players.get(j);
                other.collideWithEffects(player);
            }

            player.grenade.collideWithTilemap(tilemap);
            tilemap.collideWithAgent(player);

            // Cleanup
            if (player.health < 0) {
                player.velocity.set(0, 0);
                player.respawn(applet.random(80, applet.width - 80), applet.random(80, applet.height - 80));
            }
        }
    }

    public void handleInput() {
        // Change Level
        if (NeonPulse.g_input.isKeyPressed('L')) {
            nextLevel();
            load();
        }
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);

        canvas.translate(tilemap.left, tilemap.top);

        tilemap.display(canvas);

        for (Player player : players) {
            player.display(canvas);
        }

        canvas.endDraw();
        return canvas;
    }

    public void unload() {
        players.clear();
    }

    public void addPlayer(Player player) {
        player.position.set(applet.random(80, applet.width - 80), applet.random(80, applet.height - 80));
        super.addPlayer(player);
    }

    private void nextLevel() {
        current_map_index = (current_map_index + 1) % MAPS.length;
    }
}
