import engine.*;
import network.*;
import util.*;
import effects.*;
import game.*;

import java.util.ArrayList;

// Controllers
import org.gamecontrolplus.*;

// Networking
import processing.net.*;
import processing.sound.AudioDevice;

// PostFX
import ch.bildspur.postprocessing.builder.*;
import ch.bildspur.postprocessing.pass.*;
import ch.bildspur.postprocessing.*;

// Debug
Debug g_debug;

// Networking
Server g_game_server;

// Controllers
ControlIO g_control_io;
Configuration g_ps3_controller_config;
ArrayList<Configuration> g_controller_configs = new ArrayList<Configuration>(3);

// Global / Keyboard Input
Input g_input = new Input();

// PostFX
private PostFX fx;
private boolean g_post_processing = false;

// Audio
private AudioDevice g_audio_server;

// Timing
private int g_millis = 0;

// Screens
private ArrayList<Screen> g_screens = new ArrayList<Screen>(3);
private int g_current_screen_index = 2;

static final class Config {
  static final boolean DEBUG = true;
  static final boolean KEYBOARD = true;
  static final int PORT = 5204;
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

  void end() {
    graphics.endDraw();
  }
}

public void setup() {
    // Environment
    frameRate(60);
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

    // PostFX
    fx = new PostFX(this);
    fx_supervisor = new PostFXSupervisor(this);

    // Screens
    screens.add(new TitleScreen(this));
    screens.add(new GameScreen(this));
    screens.add(new ShaderScreen(this, fx_supervisor));
    screens.add(new TiledScreen(this));
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
    PGraphics canvas = currentScreen.render();
    blendMode(BLEND);
    image(canvas, 0, 0);

    if (postProcessingActive) {
        blendMode(SCREEN);
        currentScreen.renderFX(fx);
    }

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
  int mouse_index = Input.getButtonIndex(mouseButton);
  if (mouse_index < 0) return;
  g_input.curr_button_state[mouse_index] = true;
}

public void mouseReleased() {
  int mouse_index = Input.getButtonIndex(mouseButton);
  if (mouse_index < 0) return;
  g_input.curr_button_state[mouse_index] = false;
}

// TODO: Server object already keeps list of clients, rework server stuff
public void serverEvent(Server server, Client client) {
  Screen.addClient(client);
}

public void settings() {
  size(1024, 576, P3D);
}