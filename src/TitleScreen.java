import engine.Screen;
import util.Pair;
import util.Terrain;
import processing.core.*;

import java.util.ArrayList;

import static processing.core.PConstants.*;

class TitleScreen extends Screen {
    // Fonts
    private PFont rubber;
    private PGraphics canvas;
    private PImage background;

    // Maps
    private ArrayList<Pair<PVector, String>> maps;
    private PImage map1;
    private PImage map2;
    private PImage map3;

    private static final float BUTTON_WIDTH  = 512;
    private static final float BUTTON_HEIGHT = 288;

    // Screens
    private final GameScreen gameScreen;
    private final ScreenState screenState;

    // Intro and Background
    private Terrain mountains;
    private Terrain floor;
    private Sun sun;
    private PVector pulsePoint;

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

        void display(PGraphics g) {
            g.ellipse(position.x, position.y, radius * 2, radius * 2);
        }
    }

    TitleScreen(PApplet applet, GameScreen game_screen) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);
        screenState = ScreenState.INTRO;

        // Background
        background = applet.loadImage("art/bg3.jpg");
        background.resize(applet.width, applet.height);

        // Terrain
//        mountains = new Terrain(applet);
//        floor = new Terrain(applet);

        // Sun
//        sun = new Sun(applet.width / 2, applet.height / 2);

        // Text
        rubber = applet.createFont("fonts/rubber.ttf",72);

        // Pulse
        pulsePoint = new PVector(0, 0);

        this.gameScreen = game_screen;

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
    }

    @Override
    public void handleInput() {
        PVector m = NeonPulse.g_input.getMousePosition();
        if(NeonPulse.g_input.isButtonPressed(LEFT)) {
            for (Pair<PVector, String> map : maps) {
                if( m.x >= map.first.x
                        && m.x <= map.first.x + BUTTON_WIDTH
                        && m.y >= map.first.y
                        && m.y <= map.first.y + BUTTON_HEIGHT) {
                    NeonPulse.goToScreen(gameScreen);
                    gameScreen.loadMap(map.second);
                    gameScreen.loadPlayers();
                    return;
                }
            }
        }
    }

    public void update(float deltatime) {
        pulsePoint.x = (pulsePoint.x + deltatime * PULSE_SPEED) % applet.width;
//         mountains.update();
//         floor.update();
//         sun.update();

        switch (screenState) {
            case INTRO:
            case TITLE:
            case MAP:
        }
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0xffffffff);

         canvas.image(background, 0, 0);
//         canvas.image(mountains.render(), 0, 0);
//         canvas.image(floor.render(), 0, 0);
//
//         sun.display(canvas);

        canvas.pushStyle();
        canvas.textAlign(CENTER);
        canvas.strokeWeight(4);
        canvas.stroke(0xffffffff);
        canvas.textFont(rubber);
        canvas.point(pulsePoint.x, applet.height / 5 + 16);

        switch (screenState) {
            case INTRO:
                break;
            case TITLE:
                canvas.text("Neon Pulse", applet.width / 2, applet.height / 4);
                break;
            case MAP:
                canvas.text("Select Map", canvas.width/2, canvas.height / 5);

                canvas.image(map1,100, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
                canvas.image(map2,700, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
                canvas.image(map3,1300, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
            break;
        }

        canvas.popStyle();
        canvas.endDraw();


        return canvas;
    }

    public void unload() {
        // Finish
    }
}