import ch.bildspur.postfx.builder.PostFX;
import effects.Area;
import effects.Pulse;
import game.*;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import engine.Screen;
import engine.Tilemap;

import java.util.List;

import static processing.core.PApplet.parseInt;
import static processing.core.PConstants.*;

public class GameScreen extends Screen {
    private final PGraphics canvas;
    private Tilemap tilemap;
    private String currentMapPath = MAPS[0];
    private float roundTimer;

    private static final float ROUND_TIME = 120;
    private static final String[] MAPS = {
            "map1.tmx",
            "map2.tmx",
            "map3.tmx",
    };

    GameScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
    }

    public void load() {
        loadMap(currentMapPath);
        loadPlayers();
    }

    public void loadMap(String path) {
        currentMapPath = path;
        tilemap = new Tilemap(applet, path);
        roundTimer = ROUND_TIME;
    }

    void loadPlayers() {
        players.clear();

        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(applet, new KeyboardInput(NeonPulse.g_input), NeonPulse.Debug.test_sound);
            player.addEffect("R", new Area(NeonPulse.Debug.test_sound));
            player.addEffect("E", new Pulse(NeonPulse.Debug.test_sound));
            addPlayer(player);
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.g_control_io.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.g_controller_configs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(applet, new GamepadInput(gamepad), NeonPulse.Debug.test_sound);
                    player.addEffect("CIRCLE", new Area(NeonPulse.Debug.test_sound));
                    player.addEffect("TRIANGLE", new Pulse(NeonPulse.Debug.test_sound));
                    addPlayer(player);
                    break;
                }
            }
        }
    }

    public void handleInput() {
        // Change Level
        if (NeonPulse.g_input.isKeyPressed('L')) nextRound();
    }

    public void update(float delta_time) {
        tilemap.update(delta_time * 1000);

        for (int i = 0; i < players.size(); i++) {
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
                if (i == j) continue;
                Player other = players.get(j);
                other.collideWithEffects(player);
                if (player.health != Player.HEALTH) {
                    other.score += 1;
                }
            }

            player.grenade.collideWithTilemap(tilemap);
            player.laser.collideWithTilemap(tilemap);
            tilemap.collideWithAgent(player);

            // Cleanup
            if (player.alive && player.health < 0) {
                player.respawn(tilemap.getSpawnPoint());
            }
        }

        if (roundTimer < 0) nextRound();
        roundTimer -= delta_time;
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);

        canvas.pushMatrix();
        canvas.translate(tilemap.left, tilemap.top);

        tilemap.display(canvas);

        for (Player player : players) {
            player.display(canvas);
        }

        canvas.popMatrix();

        canvas.textAlign(CENTER);
        canvas.textSize(36);
        canvas.text(parseInt(roundTimer), canvas.width/2, 36);

        // Player Scores
        for (int i = 0; i < players.size(); i++) {
            canvas.pushStyle();
            canvas.fill(0xff000000, 127);
            canvas.rect(canvas.width/6 * (i + 1) - 18, 4, 36, 36);
            canvas.fill(Player.PLAYER_COLORS[i]);
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
    }

    public void addPlayer(Player player) {
        player.setFill(Player.PLAYER_COLORS[players.size()]);
        player.position.set(applet.random(80, applet.width - 80), applet.random(80, applet.height - 80));
        super.addPlayer(player);
    }

    private void nextRound() {}
}
