import engine.Screen;

import processing.core.*;
import util.Pair;

import java.util.ArrayList;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.P2D;

public class MenuScreen extends Screen {
    private PImage background;
    private PGraphics canvas;
    private ArrayList<Pair<PVector, String>> maps;
    private PImage map1;
    private PImage map2;
    private PImage map3;
    private PFont rubber;

    private final GameScreen game_screen;

    private static final float BUTTON_WIDTH  = 512;
    private static final float BUTTON_HEIGHT = 288;

    protected MenuScreen(PApplet applet, GameScreen screen) {
        super(applet);
        this.game_screen = screen;

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

    @Override
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
                    NeonPulse.goToScreen(game_screen);
                    game_screen.loadMap(map.second);
                    game_screen.loadPlayers();
                    return;
                }
            }
        }
    }

    @Override
    public void update(float deltatime) {

    }

    @Override
    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);

        canvas.image(background, 0, 0);

        canvas.textAlign(CENTER);
        canvas.textFont(rubber);
        canvas.text("Select Map", canvas.width/2, canvas.height / 5);
        
        canvas.image(map1,100, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
        canvas.image(map2,700, 420, BUTTON_WIDTH, BUTTON_HEIGHT);
        canvas.image(map3,1300, 420, BUTTON_WIDTH, BUTTON_HEIGHT);

        canvas.endDraw();
        return canvas;
    }

    @Override
    public void unload() {

    }
}
