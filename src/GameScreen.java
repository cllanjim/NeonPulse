import effects.Area;
import effects.Beam;
import effects.Cone;
import effects.Pulse;
import game.*;
import org.gamecontrolplus.Configuration;
import processing.sound.SoundFile;
import engine.Input;
import engine.*;
import engine.Level;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.net.Client;
import engine.ImageTile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static processing.core.PConstants.*;

class GameScreen extends Screen {
    private Level level;
    private int current_level_index = 0;
    private int current_spawn_point = 0;
    private PGraphics canvas;
    private ArrayList<Mob> mobs;

    private PImage wall_texture;
    private PImage floor_texture;
    private PImage smoke_texture;
    private PImage pit_texture;

    private static SoundFile test_sound;

    // Constants
    private static final float TILE_SIZE = 32;
    private static final int NUM_MOBS = 1;

    GameScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        smoke_texture = applet.loadImage("texture.png");
        wall_texture = applet.loadImage("tiles/wall.png");
        floor_texture = applet.loadImage("tiles/floor.png");
        pit_texture = applet.loadImage("tiles/pit.png");
        test_sound = new SoundFile(applet,"audio/test.wav");
        mobs = new ArrayList<>(NUM_MOBS);
    }

    public void load() {
        // Clear state
        players.clear();
        mobs.clear();

        // Load Tileset
        // TODO: Load TSX file to get tile data
        HashMap<Character, Tile> tile_map = new HashMap<>();
        tile_map.put('#', new ImageTile(wall_texture, true));
        tile_map.put(' ', new ImageTile(floor_texture, false));
        tile_map.put('X', new ImageTile(pit_texture, false));

        // Load Level
        level = new Level(applet, LEVELS[current_level_index], tile_map, TILE_SIZE);

//        addMobs(NUM_MOBS);

        // TODO: Player Lobby in Title screen
        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(new KeyboardInput(NeonPulse.g_input), test_sound);
            player.addEffect("F", new Beam(test_sound));
            player.addEffect("R", new Cone(test_sound));
            player.addEffect("E", new Pulse(test_sound));
            player.addEffect("X", new Area(test_sound));
            addPlayer(player);
        }

        // Load Network players
        for (Client network_client : clients) {
            Player player = new Player(new NetworkInput(new Input(), network_client), test_sound);
            player.addEffect("F", new Beam(test_sound));
            player.addEffect("R", new Cone(test_sound));
            player.addEffect("E", new Pulse(test_sound));
            player.addEffect("X", new Area(test_sound));
            addPlayer(player);
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.g_control_io.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.g_controller_configs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(new GamepadInput(gamepad), test_sound);
                    player.addEffect("LEFT_SHOULDER", new Beam(test_sound));
                    player.addEffect("CIRCLE", new Cone(test_sound));
                    player.addEffect("TRIANGLE", new Pulse(test_sound));
                    player.addEffect("LEFT_TRIGGER", new Area(test_sound));
                    addPlayer(player);
                    break;
                }
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

    public void update(float delta_time) {
        level.update(delta_time);
        updatePlayers(delta_time);
    }

    private void updatePlayers(float delta_time) {
        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            player.update(players, delta_time);

            // Collide current player with others, starting with the one after it
            for (int j = i + 1; j < players.size(); j++) {
                Player other = players.get(j);
                player.collideWithAgent(other);
            }
        }

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            // Collide with every player's effects.
            for (int j = 0; j < players.size(); j++) {
                // Don't collide with own stuff.
                if (i == j) continue;
                Player other = players.get(j);
                other.collideWithEffects(player);
            }

        }

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            player.updateMovement(delta_time);

            // Cleanup
            if (player.health < 0) {
                player.velocity.set(0, 0);
                player.respawn(applet.random(80, applet.width - 80), applet.random(80, applet.height - 80));
            }

            player.grenade.collideWithLevel(level);
            level.collideWithAgent(player);
        }
    }
    
    public PGraphics render() {
        // Draw
        canvas.beginDraw();
        canvas.background(0);
        level.showBg(canvas);

        for (Player player : players) {
            player.display(canvas);
        }

        for (Mob mob : mobs) {
            mob.display(canvas);
        }

        level.showFg(canvas);
        canvas.endDraw();

        return canvas;
    }

    public void unload(Screen next_screen) {
        players.clear();
        mobs.clear();
        level.unload();
    }

    public void addPlayer(Player player) {
        player.position.set(level.player_spawn_points.get(current_spawn_point));
        current_spawn_point = (current_spawn_point + 1) % level.player_spawn_points.size();
        super.addPlayer(player);
    }

    private void nextLevel() {
        current_level_index = (current_level_index + 1) % LEVELS.length;
    }

    // Test levels
    // '#' - Wall
    // 'X' - Pit
    // ' ' - Floor
    private static final String[] LEVEL_1 = {
            "################################",
            "#1                            3#",
            "#                              #",
            "# #####           #####        #",
            "#                              #",
            "#                              #",
            "#     ####    XXXX    ####     #",
            "#             XXXX             #",
            "#             XXXX             #",
            "#             XXXX             #",
            "#             XXXX             #",
            "#     ####    XXXX    ####     #",
            "#                              #",
            "#                              #",
            "#        #####           ##### #",
            "#                              #",
            "#4                            2#",
            "################################",
    };

    private static final String[] LEVEL_2 = {
            "################################",
            "#1     XXXXXXXXXXXXXXXXXX     3#",
            "#        XXXXXXXXXXXXXX        #",
            "#                              #",
            "#                              #",
            "#                              #",
            "#X                            X#",
            "#XXX        XXXXXXXX        XXX#",
            "#XXX           XX           XXX#",
            "#XXX           XX           XXX#",
            "#XXX        XXXXXXXX        XXX#",
            "#X                            X#",
            "#                              #",
            "#                              #",
            "#                              #",
            "#        XXXXXXXXXXXXXX        #",
            "#4     XXXXXXXXXXXXXXXXXX     2#",
            "################################",
    };

    private static final String[] LEVEL_3 = {
            "################################",
            "#1                            3#",
            "#           ########          3#",
            "#           #      #           #",
            "#           #      #           #",
            "#           #      #           #",
            "#           #      #           #",
            "#           ###  ###           #",
            "#                              #",
            "#   ###  ###        ###  ###   #",
            "#   #      #        #      #   #",
            "#   #      #  XXXX  #      #   #",
            "#   #      #  XXXX  #      #   #",
            "#   #      #        #      #   #",
            "#   ########        ########   #",
            "#                              #",
            "#4                            2#",
            "################################",
    };

    private static final String[][] LEVELS = {
            LEVEL_1,
            LEVEL_2,
            LEVEL_3
    };
}
