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
  frameRate(60 );
  noStroke();
  noCursor();

  // Controllers
  g_control_io = ControlIO.getInstance(this);
  g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps3"));
  g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_ps4"));
  g_controller_configs.add(Configuration.makeConfiguration(this, "config/gamepad_xbox"));

  // Debug
  g_debug = new Debug(this);

  // Screens
  g_screens.add(new TitleScreen(this));
  g_screens.add(new GameScreen(this));
  g_screens.add(new TiledScreen(this));

  g_screens.get(g_current_screen_index).load(g_input, g_control_io, g_controller_configs);

  // Networking
  try {
    g_game_server = new Server(this, Config.PORT);
  } 
  catch (Exception e) {
    println(e);
  }

  // PostFX
  fx = new PostFX(this);

  // Load Music
  g_audio_server = new AudioDevice(this, 44100, 128);

  // Avoid time skip on first draw
  g_millis = millis();
}

public void draw() {
  // Timing
  int current_millis = millis();
  float deltatime = (current_millis - g_millis) / 1000.0f;
  g_millis = current_millis;

  // Begin Debug
  if (Config.DEBUG) {
    g_debug.begin();
    g_debug.drawFPS(0, 20);
    g_debug.drawCursor(mouseX, mouseY);
  }

  // Run Screen
  Screen current_screen = g_screens.get(g_current_screen_index);
  current_screen.handleInput(g_input, g_control_io, g_controller_configs);
  current_screen.handleEvents(g_game_server);
  current_screen.update(deltatime);

  // Render Screen
  PGraphics canvas = current_screen.render();

  blendMode(BLEND);
  image(canvas, 0, 0);

  // TODO: Put somewhere else
  // TODO: Pass supervisor into screens for granular special effects.
  if (g_post_processing) {

    blendMode(SCREEN);             // linear interpolation of colours: C = A*factor + B.
    // blendMode(ADD);             // additive blending with white clip: C = min(A*factor + B, 255)
    // blendMode(SUBTRACT);        // subtractive blending with black clip: C = max(B - A*factor, 0)
    // blendMode(DARKEST);         // only the darkest colour succeeds: C = min(A*factor, B)
    // blendMode(LIGHTEST);        // only the lightest colour succeeds: C = max(A*factor, B)
    // blendMode(DIFFERENCE);      //  subtract colors from underlying image.
    // blendMode(EXCLUSION);       // similar to DIFFERENCE, but less extreme.
    // blendMode(MULTIPLY);        // multiply the colors, result will always be darker.
    // blendMode(SCREEN);          //  opposite multiply, uses inverse values of the colors.
    // blendMode(REPLACE);         // the pixels entirely replace the others and don't utilize alpha (transparency) values
    fx.render(canvas)
      .sobel()
      //.brightnessContrast(0, 1.0f)
      //.pixelate(512)
      //.chromaticAberration()
      //.bloom(1, 1, 1)
      //.vignette(1, 1)
      //.noise(1, 1)
      //.saturationVibrance(0, 0)
      //.blur(5, 50)
      .compose();
    blendMode(BLEND);
  }


  // Render Debug
  if (Config.DEBUG) {
    g_debug.end();
    image(g_debug.graphics, 0, 0);
  }

  // Global handlers
  if (g_input.isKeyPressed('P')) {
    save("screenshot.png");
  }

  if (g_input.isKeyPressed('U')) {
    g_post_processing = !g_post_processing;
  }

  // TODO: put somewhere else
  if (g_input.isKeyPressed('O')) {
    current_screen.load(g_input, g_control_io, g_controller_configs);
  }

  if (g_input.isKeyPressed('I')) {
    g_current_screen_index = (g_current_screen_index + 1) % g_screens.size();
    g_screens.get(g_current_screen_index).load(g_input, g_control_io, g_controller_configs);
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