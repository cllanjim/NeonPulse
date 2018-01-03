import ch.bildspur.postfx.builder.PostFX;
import effects.Area;
import effects.Pulse;
import game.*;
import org.gamecontrolplus.Configuration;
import processing.core.PVector;
import engine.*;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import util.Boxes;

import java.util.HashMap;
import java.util.List;

import static processing.core.PApplet.parseInt;
import static processing.core.PConstants.*;

class TestScreen extends Screen {
    private StringMap level;
    Player testPlayer;

    private int currentLevelIndex = 0;
    private final PGraphics canvas;
    private final Lighting lighting;
    private final PGraphics background;

    private Boxes b;

    private float roundTimer;

    private HashMap<Character, Tile> tileMap = new HashMap<>();

    // Constants
    private static final int TILE_SIZE = 32;
    private static final float ROUND_TIME = 30;

    TestScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        background = applet.createGraphics(applet.width, applet.height, P2D);
        b = new Boxes(applet);

        // Images
        PImage smoke_texture = applet.loadImage("texture.png");
        PImage wall_texture = applet.loadImage("tiles/wall.png");
        PImage floor_texture = applet.loadImage("tiles/floor.png");
        PImage pit_texture = applet.loadImage("tiles/pit.png");

        lighting = new Lighting(applet, smoke_texture);

        // Tiles
        tileMap.put('#', new Tile(wall_texture, true));
        tileMap.put(' ', new Tile(floor_texture, false));
        tileMap.put('X', new Tile(pit_texture, false));
    }

    public void load() {
        loadLevel();
        loadPlayers();
    }

    protected void loadLevel() {
        level = new StringMap(applet, LEVELS[currentLevelIndex], tileMap, TILE_SIZE);
        roundTimer = ROUND_TIME;
    }

    protected void loadPlayers() {
        players.clear();
        lighting.clear();

        // TODO: Player Lobby in Title game_screen
        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(applet, new KeyboardInput(NeonPulse.g_input), NeonPulse.Debug.test_sound);
            player.addEffect("E", new Pulse(NeonPulse.Debug.test_sound));
            player.addEffect("R", new Area(NeonPulse.Debug.test_sound));
            addPlayer(player);
            testPlayer = player;
            lighting.addLight(player.light);
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.g_control_io.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.g_controller_configs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(applet, new GamepadInput(gamepad), NeonPulse.Debug.test_sound);
                    player.addEffect("TRIANGLE", new Pulse(NeonPulse.Debug.test_sound));
                    player.addEffect("CIRCLE", new Area(NeonPulse.Debug.test_sound));
                    addPlayer(player);
                    lighting.addLight(player.light);
                    break;
                }
            }
        }
    }

    public void handleInput() {
        if (NeonPulse.g_input.isKeyPressed('L')) {
            nextLevel();
        }

        if (NeonPulse.g_input.isKeyPressed('K')) {
            // Change Level
            testPlayer.damageLethal(10);
        }
    }

    public void update(float delta_time) {
        b.update(delta_time);
        level.update(delta_time);
        updatePlayers(delta_time);
        roundTimer -= delta_time;

        if (roundTimer < 0) nextLevel();
    }

    private void updatePlayers(float delta_time) {
        // TODO: Put all effects into a single managed array
        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            // Collide with every player's effects.
            for (int j = 0; j < players.size(); j++) {
                // Don't collide with own stuff.
                if (i == j) continue;
                Player other = players.get(j);
                other.collideWithEffects(player);
                if (player.health != Player.HEALTH) {
                    other.score += 1;
                }
            }
        }

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            player.update(players, delta_time);
            player.updateMovement(delta_time);

            // Collide current player with others, starting with the one after it
            for (int j = i + 1; j < players.size(); j++) {
                Player other = players.get(j);
                player.collideWithAgent(other);
            }

            player.light.setPosition(player.position);

            // Cleanup
            if (player.alive && player.health < Player.HEALTH) {
                player.state = new Player.KilledState(level.getSpawnPoint());
                player.alive = false;
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

        b.display(canvas);

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

        // Lighting
        lighting.display(canvas);

        // Mouse
        canvas.pushStyle();
        PVector mouse = NeonPulse.g_input.getMousePosition();
        canvas.stroke(0xffffffff);
        canvas.strokeWeight(2);
        canvas.point(mouse.x, mouse.y);
        canvas.popStyle();

        canvas.popMatrix();

        // Round Timer
        canvas.textAlign(CENTER);
        canvas.textSize(36);
        canvas.text(parseInt(roundTimer), canvas.width/2, 36);

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
        player.position.set(level.getSpawnPoint());
        lighting.addLight(player.light);
        super.addPlayer(player);
    }

    private void nextLevel() {
        currentLevelIndex = (currentLevelIndex + 1) % LEVELS.length;
        loadLevel();
    }

    // Test levels
    // '#' - Wall
    // 'X' - Pit
    // ' ' - Floor
    private static final String[] LEVEL_1 = {
            "     #############################################     ",
            "                                                       ",
            "   1                                               3   ",
            "        ####                               ####        ",
            "#                                                     #",
            "#                                                     #",
            "#    ####     XXXXXXXXXXXXXXXXXXXXXXXXXXX     ####    #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "            XXX                         XXX            ",
            "                                                       ",
            "     ####                                     ####     ",
            "     ####                                     ####     ",
            "                                                       ",
            "            XXX                         XXX            ",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#             X                         X             #",
            "#    ####     XXXXXXXXXXXXXXXXXXXXXXXXXXX     ####    #",
            "#                                                     #",
            "#                                                     #",
            "        ####                               ####        ",
            "   4                                               2   ",
            "                                                       ",
            "     #############################################     ",
    };

    private static final String[] LEVEL_2 = {
            "##################                   ##################",
            "#                #                   #                #",
            "# 1              #     #########     #              3 #",
            "#                #     #       #XXXXX#                #",
            "#                #     #       #XXXXX#                #",
            "#                #     #       #######                #",
            "#                                                     #",
            "#                                                     #",
            "#                                                     #",
            "########              XXXXXXXXXXX              ########",
            "        #                  X                           ",
            "         #                 X                           ",
            "         X                 X                           ",
            "         #                 X                           ",
            "        #                  X                           ",
            "########                   X                   ########",
            "                           X                  #        ",
            "                           X                 #         ",
            "                           X                 X         ",
            "                           X                 #         ",
            "                           X                  #        ",
            "########              XXXXXXXXXXX              ########",
            "#                                                     #",
            "#                                                     #",
            "#                                                     #",
            "#                #######       #     #                #",
            "#                #XXXXX#       #     #                #",
            "#                #XXXXX#       #     #                #",
            "# 4              #     #########     #              2 #",
            "#                #                   #                #",
            "##################                   ##################",
    };

    private static final String[] LEVEL_3 = {
            "      ###########################################      ",
            "                          XXX                          ",
            "                          XXX                          ",
            "      1                   XXX                   3      ",
            "                          XXX                          ",
            "                          XXX                          ",
            "                                                       ",
            "#                                                     #",
            "#                                                     #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#XXXXXXXXXXXXXX   XXXXXXXXXXXXXXXXXXX   XXXXXXXXXXXXXX#",
            "#XXXXXXXXXXXXXX   XXXXXXXXXXXXXXXXXXX   XXXXXXXXXXXXXX#",
            "#XXXXXXXXXXXXXX   XXXXXXXXXXXXXXXXXXX   XXXXXXXXXXXXXX#",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                         XXX                         #",
            "#                                                     #",
            "#                                                     #",
            "#                         XXX                         #",
            "                          XXX                          ",
            "                          XXX                          ",
            "                          XXX                          ",
            "      4                   XXX                   2      ",
            "                          XXX                          ",
            "                          XXX                          ",
            "      ###########################################      ",
    };

    private static final String[][] LEVELS = {
            LEVEL_1,
            LEVEL_2,
            LEVEL_3
    };
}
