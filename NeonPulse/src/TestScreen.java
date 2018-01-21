import ch.bildspur.postfx.builder.PostFX;
import engine.GameScreen;
import engine.Lighting;
import engine.StringMap;
import engine.Tile;
import game.GamepadInput;
import game.KeyboardInput;
import game.Player;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.HashMap;
import java.util.List;

import static processing.core.PApplet.parseInt;
import static processing.core.PConstants.*;

class TestScreen extends GameScreen {
    private StringMap level;

    private int currentLevelIndex = 0;
    private final PGraphics canvas;
    private final Lighting lighting;

    private float roundTimer;

    private final HashMap<Character, Tile> tileMap = new HashMap<>();

    // Constants
    private static final int TILE_SIZE = 32;
    private static final float ROUND_TIME = 300;

    TestScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);

        // Images
        PImage smoke_texture = applet.loadImage("texture.png");
        PImage wall_texture = applet.loadImage("grid/wall.png");
        PImage floor_texture = applet.loadImage("grid/floor.png");
        PImage pit_texture = applet.loadImage("grid/pit.png");

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

    private void loadLevel() {
        level = new StringMap(applet, LEVELS[currentLevelIndex], tileMap, TILE_SIZE);
        roundTimer = ROUND_TIME;
    }

    protected void loadPlayers() {
        players.clear();
        lighting.clear();

        // TODO: Player Lobby in Title game_screen
        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            addPlayer(new Player(applet, new KeyboardInput(NeonPulse.sInputState), NeonPulse.Debug.testSound, lighting));
            addPlayer(new Player(applet, new KeyboardInput(NeonPulse.sInputState), NeonPulse.Debug.testSound, lighting));
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.sControlIO.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.sControllerConfigs) {
                if (gamepad.matches(configuration)) {
                    addPlayer(new Player(applet, new GamepadInput(gamepad), NeonPulse.Debug.testSound, lighting));
                    break;
                }
            }
        }
    }

    public void handleInput() {
        if (NeonPulse.sInputState.isKeyPressed('L')) {
            nextLevel();
        }
        for (Player player: players) {
            player.handleInput();
        }
    }

    public void update(float delta_time) {
        level.update(delta_time);

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);

            // Kill if in pit
            if (player.alive && level.checkPositionFor(player.position.x, player.position.y, 'X')) {
                player.impulse.set(0, 0);
                if (player.velocity.mag() <= player.speed * 8f) {
                    player.kill(level.getSpawnPoint());
                    player.score -= 1;
                }
            }

            for (int j = 0; j < players.size(); j++) {
                if (i == j) continue;

                Player other = players.get(j);
                other.collideWithEffects(player);
                if(player.alive && player.health != Player.HEALTH) {
                    other.score += 1;
                    player.kill(level.getSpawnPoint());
                }
            }
        }

        for (int i = 0; i < players.size(); i++ ) {
            Player player = players.get(i);
            player.update(level, delta_time);
            player.updateMovement(delta_time);

            // Collide current player with others, starting with the one after it
            for (int j = i + 1; j < players.size(); j++) {
                Player other = players.get(j);
                player.collideWithAgent(other);
            }

            player.updateLights(delta_time, player.position.x + level.offsetX, player.position.y + level.offsetY, level);

            level.wrapAgent(player);
            level.collideWithPlayer(player);
        }

        if (roundTimer < 0) nextLevel();
        roundTimer -= delta_time;
    }

    public PGraphics render() {
        // Draw
        canvas.beginDraw();
        canvas.background(0);

        // Level Background
        level.showBg(canvas);

        // Players
        canvas.pushMatrix();
        canvas.translate(level.offsetX, level.offsetY);
        for (Player player : players) {
            player.display(canvas);
        }
        level.showFg(canvas);
        canvas.popMatrix();

        // Lighting
        lighting.display(canvas);

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
        fx.render(canvas).sobel().blur(5, 10).compose();
        applet.blendMode(BLEND);
    }

    public void unload() {
        players.clear();
        level.unload();
    }

    public void addPlayer(Player player) {
        player.setFill(Player.PLAYER_COLORS[players.size() % Player.PLAYER_COLORS.length]);
        player.position.set(level.getSpawnPoint());
        lighting.addLights(player.lights);
        super.addPlayer(player);
    }

    private void nextLevel() {
        currentLevelIndex = (currentLevelIndex + 1) % LEVELS.length;
        loadLevel();
        for (Player player : players) {
            player.respawn(level.getSpawnPoint());
        }
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
            "#     1          #     #########     #          3     #",
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
            "#     4          #     #########     #          2     #",
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
