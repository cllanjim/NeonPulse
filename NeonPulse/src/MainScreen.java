import ch.bildspur.postfx.builder.PostFX;
import engine.GameScreen;
import engine.Lighting;
import engine.Tilemap;
import game.GamepadInput;
import game.KeyboardInput;
import game.Player;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.List;

import static processing.core.PApplet.parseInt;
import static processing.core.PConstants.*;

public class MainScreen extends GameScreen {
    private final PGraphics canvas;
    private Tilemap tilemap;
    private int currentMapIndex = 0;
    private float roundTimer;

    private final Lighting lighting;

    private static final float ROUND_TIME = 120;
    private static final String[] MAPS = {
            "map1.tmx",
            "map2.tmx",
            "map3.tmx",
    };

    MainScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);

        PImage smoke_texture = applet.loadImage("texture.png");
        lighting = new Lighting(applet, smoke_texture);
    }

    public void load() {
        loadMap(currentMapIndex);
        loadPlayers();
    }

    public void loadMap(int index) {
        tilemap = new Tilemap(applet, MAPS[currentMapIndex]);
        roundTimer = ROUND_TIME;
    }

    void loadPlayers() {
        players.clear();
        lighting.clear();

        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            Player player = new Player(applet, new KeyboardInput(NeonPulse.sInputState), NeonPulse.Debug.testSound, lighting);
            addPlayer(player);
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.sControlIO.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.sControllerConfigs) {
                if (gamepad.matches(configuration)) {
                    Player player = new Player(applet, new GamepadInput(gamepad), NeonPulse.Debug.testSound, lighting);
                    addPlayer(player);
                    break;
                }
            }
        }
    }

    public void handleInput() {
        // Change Level
        if (NeonPulse.sInputState.isKeyPressed('L')) nextRound();
        if (NeonPulse.sInputState.isKeyPressed('K')) lighting.active = !lighting.active;

        for (Player player : players) {
            player.handleInput();
        }
    }

    public void update(float delta_time) {
        tilemap.update(delta_time * 1000);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            // Kill if over pit
            if (player.alive && tilemap.checkPitCollision(player.position.x, player.position.y)) {
                player.impulse.set(0, 0);
                float current_speed = player.velocity.mag();
                if (current_speed <= player.speed  * 3f) {
                    player.kill(tilemap.getSpawnPoint());
                    player.score -= 1;
                }
            }

            for (int j = 0; j < players.size(); j++) {

                if (i == j) continue;

                // Collide with every player's effects.
                Player other = players.get(j);
                other.collideWithEffects(player);
                if (player.alive && player.health != Player.HEALTH) {
                    player.kill(tilemap.getSpawnPoint());
                    other.score += 1;
                }
            }
        }

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.update(tilemap, delta_time);
            player.updateMovement(delta_time);

            // Collide current player with others, starting with the one after it
            for (int j = i + 1; j < players.size(); j++) {
                Player other = players.get(j);
                player.collideWithAgent(other);
            }

            player.updateLights(delta_time, player.position.x + tilemap.offsetX, player.position.y + tilemap.offsetY, tilemap);

            tilemap.wrapAgent(player);
            tilemap.collideWithPlayer(player);
        }

        lighting.update(delta_time);

        if (roundTimer < 0) nextRound();
        roundTimer -= delta_time;
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);

        canvas.pushMatrix();
        canvas.translate(tilemap.offsetX, tilemap.offsetY);

        tilemap.display(canvas);

        for (Player player : players) {
            player.display(canvas);
        }

        canvas.popMatrix();

        lighting.display(canvas);

        canvas.textAlign(CENTER);
        canvas.textSize(36);
        canvas.text(parseInt(roundTimer), 4 * canvas.width / 5, 36);

        // Player Scores
        for (int i = 0; i < players.size(); i++) {
            canvas.pushStyle();
            canvas.fill(0xff000000, 127);
            canvas.rect(canvas.width / 6 * (i + 1) - 18, 4, 36, 36);
            canvas.fill(Player.PLAYER_COLORS[i]);
            canvas.text(players.get(i).score, canvas.width / 6 * (i + 1), 36);
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
    }

    public void addPlayer(Player player) {
        player.setFill(Player.PLAYER_COLORS[players.size()]);
        player.position.set(tilemap.getSpawnPoint());
        lighting.addLights(player.lights);
        super.addPlayer(player);
    }

    private void nextRound() {
        currentMapIndex = (currentMapIndex + 1) % MAPS.length;
        loadMap(currentMapIndex);
        for (Player player : players) {
            player.respawn(tilemap.getSpawnPoint());
        }
    }
}
