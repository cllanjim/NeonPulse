import ch.bildspur.postfx.PostFXSupervisor;
import ch.bildspur.postfx.builder.PostFX;
import engine.GameScreen;
import engine.InputEmitter;
import engine.InputState;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlIO;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.sound.AudioDevice;
import processing.sound.SoundFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controllers
// Networking
// PostFX

public class NeonPulse extends PApplet {
    // Debug - Sets up a canvas on which to draw debug information
    static Debug g_debug = null;

    // Controllers
    static ControlIO g_control_io = null;
    static List<Configuration> g_controller_configs = new ArrayList<Configuration>(3);

    // Global / Keyboard Input
    static InputState g_inputState = new InputState();
    static InputEmitter g_inputEmitter = new InputEmitter();

    // PostFX
    private PostFX fx;
    private PostFXSupervisor fx_supervisor;

    // Audio
    private AudioDevice g_audio_server;

    // Timing
    private int currentMillis;

    // Screens
    private int currentScreenIndex = 0;
    private final ArrayList<GameScreen> screens = new ArrayList<GameScreen>(3);
    private static GameScreen currentScreen = null;

    // Settings
    private static boolean postProcessingActive = false;
    private static final float TARGET_FRAMERATE = 60;

    static final class Config {
        static final boolean DEBUG = true;
        static final boolean KEYBOARD = true;
        static final int PORT = 5204;
        static final Map<String, Float> values = new HashMap<String, Float>();

        static {
            values.put("AREA_RADIUS", 128f);
            values.put("AREA_FACTOR", 0f);
            values.put("AREA_LIFESPAN", 2f);
            values.put("BEAM_LENGTH", 386f);
            values.put("BEAM_LIFESPAN", 0.1f);
            values.put("CONE_ANGLE", PI/3.0f);
            values.put("CONE_FORCE", 128f);
            values.put("CONE_LIFESPAN", 1f);
            values.put("EXPLOSION_FORCE", 256f);
            values.put("EXPLOSION_RADIUS", 256f);
            values.put("EXPLOSION_LIFESPAN", 1f);
            values.put("PULSE_RADIUS", 256f);
            values.put("PULSE_FORCE", 128f);
            values.put("PULSE_LIFESPAN", 1f);
            values.put("PUSH_RANGE", 128f);
            values.put("PUSH_FORCE", 128f);
            values.put("PUSH_LIFESPAN", 128f);
        }

        static void setValue(String key, Float value) {
            values.put(key, value);
        }
        static float getValue(String key, Float default_value) {
            return values.getOrDefault(key, default_value);
        }
    }

    static class Debug {
        static SoundFile test_sound = null;
        PApplet applet;
        PGraphics graphics;

        Debug(PApplet applet) {
            this.applet = applet;
            graphics = applet.createGraphics(applet.width, applet.height);
            test_sound = new SoundFile(applet,"audio/test.wav");
        }

        void begin() {
            graphics.beginDraw();
            graphics.clear();
            graphics.noFill();
            graphics.stroke(255);
            graphics.strokeWeight(3);
        }

        void drawFPS(float x, float y) {
            graphics.text(applet.frameRate, x, y);
        }

        void drawCursor(float x, float y) {
            graphics.point(x, y);
        }

        PGraphics render() {
            graphics.endDraw();
            return graphics;
        }
    }

    public void setup() {
        // Environment
        frameRate(TARGET_FRAMERATE);
        noStroke();
        noCursor();

        // TODO: Auto-updater?
        // saveStream("local/latest", "http://raulgrell.com/latest");

        // Debug
        g_debug = new Debug(this);

        // Controllers
        g_control_io = ControlIO.getInstance(this);
        g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps3"));
        g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps4"));
        g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_xbox"));

        // Audio
        g_audio_server = new AudioDevice(this, 44100, 128);

        // PostFX
        fx = new PostFX(this);
        fx_supervisor = new PostFXSupervisor(this);

        // Screens
        MainScreen g_game_screen = new MainScreen(this);

        screens.add(new TitleScreen(this, g_game_screen));
        screens.add(g_game_screen);
        screens.add(new TestScreen(this));
        screens.add(new ShaderScreen(this, fx_supervisor));
//        screens.add(new ClientScreen(this));
//        screens.add(new ServerScreen(this));

        // Load first game_screen
        currentScreen = screens.get(currentScreenIndex);
        currentScreen.load();

        // Reduce time skip on first draw
        currentMillis = millis();
    }

    public void draw() {
        // Timing
        int current_millis = millis();
        float deltatime = (current_millis - currentMillis) / 1000.0f;
        currentMillis = current_millis;

        // Begin Debug
        if (Config.DEBUG) {
            g_debug.begin();
            g_debug.drawFPS(0, 20);
        }

        // Run GameScreen
        currentScreen.handleInput();
        currentScreen.update(deltatime);

        // Render GameScreen
        blendMode(BLEND);
        image(currentScreen.render(), 0, 0);

        if (postProcessingActive) {
            currentScreen.renderFX(fx);
        }

        // Render Debug
        if (Config.DEBUG) {
            blendMode(BLEND);
            image(g_debug.render(), 0, 0);
        }

        // Global handlers
        if (g_inputState.isKeyPressed('P')) save("screenshot.png");
        if (g_inputState.isKeyPressed('O')) reloadScreen();
        if (g_inputState.isKeyPressed('I')) loadNextScreen();
        if (g_inputState.isKeyPressed('U')) togglePostProcessing();

        // History
        g_inputState.saveInputState(mouseX, mouseY);
    }

    private static void reloadScreen() {
        currentScreen.unload();
        currentScreen.load();
    }

    private static void togglePostProcessing() {
        postProcessingActive = !postProcessingActive;
    }

    private void loadNextScreen() {
        currentScreenIndex = (currentScreenIndex + 1) % screens.size();
        goToScreen(screens.get(currentScreenIndex));
    }

    public static void goToScreen(GameScreen screen) {
        currentScreen.unload();
        currentScreen = screen;
        currentScreen.load();
    }

    public void keyPressed() {
        g_inputState.onKeyPressed(key, keyCode);
        g_inputEmitter.onKeyPressed(key, keyCode);
    }

    public void keyReleased() {
        g_inputState.onKeyReleased(key, keyCode);
        g_inputEmitter.onKeyReleased(key, keyCode);
    }

    public void mousePressed() {
        g_inputState.onButtonPressed(mouseButton);
        g_inputEmitter.onButtonPressed(mouseButton);
    }

    public void mouseReleased() {
        g_inputState.onButtonReleased(mouseButton);
        g_inputEmitter.onButtonReleased(mouseButton);
    }

    public void settings() {
        fullScreen(P2D);
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"NeonPulse"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
