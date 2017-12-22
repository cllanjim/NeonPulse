import ch.bildspur.postfx.builder.PostFX;
import effects.Area;
import effects.Pulse;
import game.*;
import network.NetworkInput;
import org.gamecontrolplus.Configuration;
import processing.sound.SoundFile;
import engine.*;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.net.Client;

import java.util.HashMap;
import java.util.List;

import static processing.core.PApplet.parseInt;
import static processing.core.PConstants.*;

class TestScreen extends Screen {
    private Level level;
    private int current_level_index = 0;
    private int current_spawn_point = 0;
    private PGraphics canvas;

//    private PImage bg;

//    private int cols = 160;
//    private int rows = 90;
//    private float offset = 0;

    private float round_timer;

    private HashMap<Character, Tile> tile_map = new HashMap<>();

    private static SoundFile test_sound;

    // Constants
    private static final float TILE_SIZE = 32;
    private static final float ROUND_TIME = 30;

    TestScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
//        bg = applet.createImage(cols, rows, RGB);

        // Images
        PImage smoke_texture = applet.loadImage("texture.png");
        PImage wall_texture = applet.loadImage("tiles/wall.png");
        PImage floor_texture = applet.loadImage("tiles/floor.png");
        PImage pit_texture = applet.loadImage("tiles/pit.png");

        // Tiles
        tile_map.put('#', new ImageTile(wall_texture, true));
        tile_map.put(' ', new ImageTile(floor_texture, false));
        tile_map.put('X', new ImageTile(pit_texture, false));

        // Sounds
        test_sound = new SoundFile(applet,"audio/test.wav");
    }

    public void load() {
        // Load Level
        level = new Level(applet, LEVELS[current_level_index], tile_map, TILE_SIZE);
        loadPlayers();
        round_timer = ROUND_TIME;
    }

    private void loadPlayers() {
        // TODO: Player Lobby in Title game_screen
        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(applet, new KeyboardInput(NeonPulse.g_input), test_sound);
            player.addEffect("E", new Pulse(test_sound));
            player.addEffect("R", new Area(test_sound));
            addPlayer(player);
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.g_control_io.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.g_controller_configs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(applet, new GamepadInput(gamepad), test_sound);
                    player.addEffect("TRIANGLE", new Pulse(test_sound));
                    player.addEffect("CIRCLE", new Area(test_sound));
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
        }
    }

    public void update(float delta_time) {
        level.update(delta_time);
        updatePlayers(delta_time);
//        offset -= 1f;
        if (round_timer < 0) nextLevel();
        round_timer -= delta_time;
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
                if (player.health != Player.HEALTH) other.score += 1;
            }

        }

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            player.updateMovement(delta_time);

            // Cleanup
            if (player.health < 0) {
                player.velocity.set(0, 0);
                player.respawn(
                        applet.random(TILE_SIZE * 2, applet.width  - TILE_SIZE * 2),
                        applet.random(TILE_SIZE * 2, applet.height - TILE_SIZE * 2));
            }

            player.grenade.collideWithLevel(level);
            player.laser.collideWithLevel(level);
            level.collideWithAgent(player);
        }
    }
    
    public PGraphics render() {
        // Draw
        canvas.beginDraw();
        canvas.background(0);

//        float y_offset = offset;
//        bg.loadPixels();
//        for (int y = 0; y < rows; y++) {
//            float x_offset = 0;
//            for (int x = 0; x < cols; x++) {
//                float noise = applet.noise(x_offset, y_offset);
//                bg.pixels[x + y * x] = applet.color(PApplet.map(noise, -1, 1, 0, 255));
//                x_offset += 1f;
//            }
//            y_offset += 1f;
//        }
//        bg.updatePixels();
//        canvas.image(bg, 0 ,0, 1920, 1080);

        canvas.pushMatrix();
        canvas.translate(level.left, level.top);

        // Level Background
        level.showBg(canvas);

        // Players
        for (Player player : players) {
            player.display(canvas);
        }

        // Level foreground
        level.showFg(canvas);

        canvas.popMatrix();


        // Round Timer
        canvas.textAlign(CENTER);
        canvas.textSize(36);
        canvas.text(parseInt(round_timer), canvas.width/2, 36);

        // Player Scores
        for (int i = 0; i < players.size(); i ++) {
            canvas.pushStyle();
            canvas.fill(0xff000000, 128);
            canvas.rect(canvas.width/6 * (i + 1) - 18, 4, 36, 36);
            canvas.fill(Player.PLAYER_COLORS[i % Player.PLAYER_COLORS.length]);
            canvas.text(players.get(i).score, canvas.width/6 * (i + 1), 36);
            canvas.popStyle();
        }

        canvas.endDraw();

        return canvas;
    }

    public void renderFX(PostFX fx) {
        applet.blendMode(SCREEN);
        fx.render(canvas).sobel().blur(5, 50).compose();
        applet.blendMode(BLEND);
    }

    public void unload() {
        players.clear();
        level.unload();
    }

    public void addPlayer(Player player) {
        player.setFill(Player.PLAYER_COLORS[players.size() % Player.PLAYER_COLORS.length]);
        player.position.set(level.player_spawns.get(current_spawn_point));
        current_spawn_point = (current_spawn_point + 1) % level.player_spawns.size();
        super.addPlayer(player);
    }

    private void nextLevel() {
        current_level_index = (current_level_index + 1) % LEVELS.length;
        level = new Level(applet, LEVELS[current_level_index], tile_map, TILE_SIZE);
        round_timer = ROUND_TIME;
    }

    // Test levels
    // '#' - Wall
    // 'X' - Pit
    // ' ' - Floor
    private static final String[] LEVEL_1 = {
            "#######################################################",
            "#1                                                   3#",
            "#                                                     #",
            "# ####                                     ####       #",
            "#                                                     #",
            "#                                                     #",
            "#    ####   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   ####    #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#           XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           #",
            "#    ####   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   ####    #",
            "#                                                     #",
            "#                                                     #",
            "#       ####                                     #### #",
            "#                                                     #",
            "#4                                                   2#",
            "#######################################################",
    };

    private static final String[] LEVEL_2 = {
            "#######################################################",
            "#1    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    3#",
            "#       XXXX                               XXXX       #",
            "#                                                     #",
            "#            #############################            #",
            "#            #############################            #",
            "#X                                                   X#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#XXX   #   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   #   XXX#",
            "#X                                                   X#",
            "#            #############################            #",
            "#            #############################            #",
            "#                                                     #",
            "#       XXXX                               XXXX       #",
            "#4    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    2#",
            "#######################################################",
    };

    private static final String[] LEVEL_3 = {
            "#######################################################",
            "#1                                                   3#",
            "#           ###############################          4#",
            "#          #                               #          #",
            "#     XXX  #                               #  XXX     #",
            "#     XXX  #                               #  XXX     #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#          #                               #          #",
            "#           #                             #           #",
            "#    ## ##                                   ## ##    #",
            "#   #     #  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX  #     #   #",
            "#   #     #  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX  #     #   #",
            "#   #     #  XXXXXXXXXXXXXXXXXXXXXXXXXXXXX  #     #   #",
            "#   #     #                                 #     #   #",
            "#    #####                                   #####    #",
            "#4                                                   2#",
            "#######################################################",
    };

    private static final String[][] LEVELS = {
            LEVEL_1,
            LEVEL_2,
            LEVEL_3
    };
}
