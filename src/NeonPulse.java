import ch.bildspur.postfx.PostFXSupervisor;
import engine.*;
import processing.core.*;

import java.util.ArrayList;

// Controllers
import org.gamecontrolplus.*;

// Networking
import processing.net.*;
import processing.sound.AudioDevice;

// PostFX
import ch.bildspur.postfx.builder.*;

public class NeonPulse extends PApplet {
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
    private PostFX fx;
    private PostFXSupervisor fx_supervisor;

    // Audio
    private AudioDevice g_audio_server;

    // Timing
    private int currentMillis = 0;

    // Screens
    private ArrayList<Screen> screens = new ArrayList<Screen>(3);
    private int currentScreenIndex = 3;
    private Screen currentScreen;

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

        PGraphics render() {
            graphics.endDraw();
            return graphics;
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
        size(1024, 576, P2D);
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
