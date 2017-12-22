import engine.*;
import network.*;
import util.*;
import effects.*;
import game.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

// Controllers
import org.gamecontrolplus.*;

// Networking
import processing.net.*;
import processing.sound.AudioDevice;

// Debug
static Debug g_debug;

// Networking
static Server g_game_server;

// Controllers
static ControlIO g_control_io;
static ArrayList<Configuration> g_controller_configs = new ArrayList<Configuration>(3);

// Global / Keyboard Input
static Input g_input = new Input();

// PostFX
static boolean postProcessingActive = false;

// Audio
private AudioDevice g_audio_server;

// Timing
private int currentMillis;

// Screens
private ArrayList<Screen> screens = new ArrayList<Screen>(3);
private int currentScreenIndex = 0;
private Screen currentScreen;

static Screen g_game_screen;

static final float TARGET_FRAMERATE = 60;

static final class Config {
    public static final boolean DEBUG = true;
    static final boolean KEYBOARD = true;
    static final int PORT = 5204;
    static HashMap<String, Float> values = new HashMap<String, Float>();

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
    PApplet applet;
    PGraphics graphics;

    Debug(PApplet applet) {
        this.applet = applet;
        graphics = applet.createGraphics(applet.width, applet.height);
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

    // Debug
    g_debug = new Debug(this);

    // Controllers
    g_control_io = ControlIO.getInstance(this);
    g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps3"));
    g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps4"));
    g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_xbox"));

    // Networking
    try {
        g_game_server = new Server(this, Config.PORT);
    } catch (Exception e) {
        println(e);
    }

    // Audio
    g_audio_server = new AudioDevice(this, 44100, 128);

    // Screens
    g_game_screen = new GameScreen(this);
    screens.add(new TitleScreen(this));
    screens.add(new MenuScreen(this));   
    screens.add(g_game_screen);
    screens.add(new TestScreen(this));
    
    currentScreen = screens.get(currentScreenIndex);
    currentScreen.load();

    // Avoid time skip on first draw
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
        g_debug.drawCursor(mouseX, mouseY);
    }

    // Run Screen
    currentScreen.handleInput();
    currentScreen.handleEvents(g_game_server);
    currentScreen.update(deltatime);

    // Render Screen
    blendMode(BLEND);
    image(currentScreen.render(), 0, 0);

    // Render Debug
    if (Config.DEBUG) {
        blendMode(BLEND);
        image(g_debug.render(), 0, 0);
    }

    // Global handlers
    if (g_input.isKeyPressed('P')) save("screenshot.png");
    if (g_input.isKeyPressed('U')) postProcessingActive = !postProcessingActive;
    if (g_input.isKeyPressed('O')) currentScreen.load();
    if (g_input.isKeyPressed('I')) {
        currentScreenIndex = (currentScreenIndex + 1) % screens.size();
        currentScreen = screens.get(currentScreenIndex);
        currentScreen.load();
    }

    // History
    g_input.saveInputState(mouseX, mouseY);
}

public void keyPressed() {
    g_input.pressKey(key);
}

public void keyReleased() {
    g_input.releaseKey(key);
}

public void mousePressed() {
    g_input.pressButton(mouseButton);
}

public void mouseReleased() {
    g_input.releaseButton(mouseButton);
}

// TODO: Server object already keeps list of clients, rework server stuff
public void serverEvent(Server server, Client client) {
    Screen.addClient(client);
}

public void settings() {
    fullScreen(P2D);
}