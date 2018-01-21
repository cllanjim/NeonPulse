import ch.bildspur.postfx.PostFXSupervisor;
import ch.bildspur.postfx.builder.PostFX;

import engine.InputState;
import engine.InputEmitter;
import engine.GameScreen;

import processing.sound.*;

import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controllers
static ControlIO sControlIO = null;
static List<Configuration> sControllerConfigs = new ArrayList<Configuration>(3);

// Global / Keyboard Input
static InputState sInputState = new InputState();
static InputEmitter sInputEmitter = new InputEmitter();

// PostFX
private PostFX postFX;
private PostFXSupervisor postFXSupervisor;

// Audio
private AudioDevice sAudioServer;

// Timing
private int currentMillis;

// Screens
private int currentScreenIndex = 0;
private final ArrayList<GameScreen> screens = new ArrayList<GameScreen>(3);
private static GameScreen sCurrentScreen = null;

// Settings

static final class Config {
    // Compile-time settings
    static final boolean KEYBOARD = true;
    static final int PORT = 5204;
    static final float TARGET_FRAME_RATE = 60;

    // Run-time settings
    static boolean postProcessingActive = true;
}

static final class Debug {
    static SoundFile testSound = null;
}

public void setup() {
    // Environment
    frameRate(Config.TARGET_FRAME_RATE);
    noStroke();
    noCursor();

    // TODO: Auto-updater?
    // saveStream("local/latest", "http://raulgrell.com/latest");

    // Controllers
    sControlIO = ControlIO.getInstance(this);
    sControllerConfigs.add(Configuration.makeConfiguration(this, "config/gamepad_ps3"));
    sControllerConfigs.add(Configuration.makeConfiguration(this, "config/gamepad_ps4"));
    sControllerConfigs.add(Configuration.makeConfiguration(this, "config/gamepad_xbox"));

    // Audio
    sAudioServer = new AudioDevice(this, 44100, 128);
    Debug.testSound = new SoundFile(this,"audio/test.wav");
    SoundFile music = new SoundFile(this, "audio/music.mp3");

    // PostFX
    postFX = new PostFX(this);
    postFXSupervisor = new PostFXSupervisor(this);

    // Screens
    MainScreen g_game_screen = new MainScreen(this);

    screens.add(new TitleScreen(this, g_game_screen));
    screens.add(g_game_screen);

    // Load first game_screen
    sCurrentScreen = screens.get(currentScreenIndex);
    sCurrentScreen.load();

    // Reduce time skip on first draw
    currentMillis = millis();
    music.play();
}

public void draw() {
    // Timing
    int current_millis = millis();
    float deltatime = (current_millis - currentMillis) / 1000.0f;
    currentMillis = current_millis;

    // Run GameScreen
    sCurrentScreen.handleInput();
    sCurrentScreen.update(deltatime);

    // Render GameScreen
    blendMode(BLEND);
    image(sCurrentScreen.render(), 0, 0);

    if (Config.postProcessingActive) {
        sCurrentScreen.renderFX(postFX);
    }

    // Global handlers
    if (sInputState.isKeyPressed('P')) save("screenshot.png");
    if (sInputState.isKeyPressed('O')) reloadScreen();
    if (sInputState.isKeyPressed('I')) loadNextScreen();
    if (sInputState.isKeyPressed('U')) togglePostProcessing();

    // History
    sInputState.saveInputState(mouseX, mouseY);
}

private static void reloadScreen() {
    sCurrentScreen.unload();
    sCurrentScreen.load();
}

private static void togglePostProcessing() {
    Config.postProcessingActive = !Config.postProcessingActive;
}

private void loadNextScreen() {
    currentScreenIndex = (currentScreenIndex + 1) % screens.size();
    goToScreen(screens.get(currentScreenIndex));
}

public static void goToScreen(GameScreen screen) {
    sCurrentScreen.unload();
    sCurrentScreen = screen;
    sCurrentScreen.load();
}

public void keyPressed() {
    sInputState.onKeyPressed(key, keyCode);
    sInputEmitter.onKeyPressed(key, keyCode);
}

public void keyReleased() {
    sInputState.onKeyReleased(key, keyCode);
    sInputEmitter.onKeyReleased(key, keyCode);
}

public void mousePressed() {
    sInputState.onButtonPressed(mouseButton);
    sInputEmitter.onButtonPressed(mouseButton);
}

public void mouseReleased() {
    sInputState.onButtonReleased(mouseButton);
    sInputEmitter.onButtonReleased(mouseButton);
}

public void settings() {
    fullScreen(P2D);
}