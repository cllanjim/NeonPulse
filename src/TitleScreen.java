import engine.GameScreen;
import processing.core.*;
import util.Pair;
import util.Terrain;

import java.util.ArrayList;

import static processing.core.PConstants.*;

class TitleScreen extends GameScreen {
    // Fonts
    private PFont rubber;
    private PGraphics canvas;
    private PImage background;

    // Maps
    private final ArrayList<Pair<PVector, String>> maps;
    private final PImage map1;
    private final PImage map2;
    private final PImage map3;

    private static final float BUTTON_WIDTH  = 512;
    private static final float BUTTON_HEIGHT = 288;

    // Screens
    private final MainScreen mainScreen;
    private ScreenState screenState;

    // Intro and Background
    private final Terrain mountains;
    private final Terrain floor;
    private final Sun sun;
    private PVector pulsePoint;
    private float timer;

    private static final float PULSE_SPEED = 1500;

    enum ScreenState {
        INTRO,
        TITLE,
        MAP
    }

    // TODO: Sun over mountains animation
    class Sun {
        PVector position;
        float radius;

        Sun(float x, float y) {
            position = new PVector(x, y);
            radius = 200;
        }

        void update(float delta_time) {}

        void display(PGraphics g) {
            g.ellipse(position.x, position.y, radius * 2, radius * 2);
        }
    }

    TitleScreen(PApplet applet, MainScreen game_screen) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        screenState = ScreenState.INTRO;

        // Background
        background = applet.loadImage("art/bg3.jpg");
        background.resize(applet.width, applet.height);

        // Terrain
        mountains = new Terrain(applet);
        floor = new Terrain(applet);

        // Sun
        sun = new Sun(applet.width / 2, applet.height / 2);

        // Text
        rubber = applet.createFont("fonts/rubber.ttf",72);

        // Pulse
        pulsePoint = new PVector(0, 0);

        this.mainScreen = game_screen;

        canvas = applet.createGraphics(applet.width, applet.height, P2D);

        background = applet.loadImage("art/bg3.jpg");
        rubber = applet.createFont("fonts/rubber.ttf",72);
        background.resize(applet.width, applet.height);

        maps = new ArrayList<>();
        maps.add(new Pair<>(new PVector(100, 360), "map1.tmx"));
        maps.add(new Pair<>(new PVector(700, 360), "map2.tmx"));
        maps.add(new Pair<>(new PVector(1300,360), "map3.tmx"));

        map1 = applet.loadImage("map1.png");
        map2 = applet.loadImage("map2.png");
        map3 = applet.loadImage("map3.png");
    }

    public void load() {
        screenState = ScreenState.INTRO;
        timer = 3;
    }

    @Override
    public void handleInput() {
        PVector m = NeonPulse.g_inputState.getMousePosition();
        if(NeonPulse.g_inputState.isButtonPressed(LEFT)) {
            for (Pair<PVector, String> map : maps) {
                if( m.x >= map.first.x
                        && m.x <= map.first.x + BUTTON_WIDTH
                        && m.y >= map.first.y
                        && m.y <= map.first.y + BUTTON_HEIGHT) {
                    NeonPulse.goToScreen(mainScreen);
                    mainScreen.loadMap(map.second);
                    mainScreen.loadPlayers();
                    return;
                }
            }
        }
    }

    public void update(float delta_time) {
        pulsePoint.x = (pulsePoint.x + delta_time * PULSE_SPEED) % applet.width;
        mountains.update();
        floor.update();
        sun.update(delta_time);

        switch (screenState) {
            case INTRO: {
                timer -= delta_time;
                if (timer < 0) {
                    screenState = ScreenState.TITLE;
                    timer = 2;
                }
                break;
            }
            case TITLE: {
                timer -= delta_time;
                if (timer < 0) {
                    screenState = ScreenState.MAP;
                }
                // TODO: Handle player creation
                break;
            }
            case MAP: {
                // TODO: Handle map selection
                break;
            }
        }
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0xffffffff);

        // Background
        canvas.image(background, 0, 0);

        // Animation
        canvas.image(mountains.render(), 0, 0);
        canvas.image(floor.render(), 0, 0);
        sun.display(canvas);

        // Screen
        canvas.pushStyle();
        canvas.textAlign(CENTER);
        canvas.strokeWeight(4);
        canvas.stroke(0xffffffff);
        canvas.textFont(rubber);
        canvas.point(pulsePoint.x, applet.height / 5 + 16);

        switch (screenState) {
            case INTRO: {
                break;
            }
            case TITLE: {
                canvas.text("Neon Pulse", applet.width / 2, applet.height / 4);
                break;
            }
            case MAP: {
                canvas.text("Select Map", canvas.width / 2, canvas.height / 5);
                canvas.image(map1, 100, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
                canvas.image(map2, 700, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
                canvas.image(map3, 1300, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
                break;
            }
        }

        canvas.popStyle();
        canvas.endDraw();

        return canvas;
    }

    public void unload() {
        // Finish
    }
}