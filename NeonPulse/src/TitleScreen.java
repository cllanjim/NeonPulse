import effects.Area;
import effects.Pulse;
import engine.GameScreen;
import game.Draw;
import game.GamepadInput;
import game.KeyboardInput;
import game.Player;
import org.gamecontrolplus.Configuration;
import org.gamecontrolplus.ControlDevice;
import processing.core.*;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PConstants.*;

class TitleScreen extends GameScreen {
    private static final int PADDING = 20;
    // Fonts
    private final PFont rubber;
    private final PGraphics canvas;
    private final PImage background;

    // Maps
    private final ArrayList<MapButton> mapButtons;
    private final PImage map1;
    private final PImage map2;
    private final PImage map3;
    private static final float BUTTON_WIDTH = 512;
    private static final float BUTTON_HEIGHT = 288;

    // Players
    private final ArrayList<PlayerButton> playerButtons;

    // Screens
    private final MainScreen mainScreen;
    private ScreenState screenState;

    // Intro and Background
    private final PVector pulsePoint;
    private float timer;

    private static final float PULSE_SPEED = 1500;

    static class PlayerButton {
        float x, y, w, h;
        Player player;

        PlayerButton(float x, float y, float w, float h, Player player) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.player = player;
        }
    }

    static class MapButton {
        float x, y, w, h;
        int index;
        String path;
        PImage image;

        MapButton(String path, float x, float y, float w, float h, PImage image, int index) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.path = path;
            this.image = image;
            this.index = index;
        }
    }

    class IntroState implements ScreenState {
        @Override
        public void handleInput() {
            if (NeonPulse.sInputState.isButtonPressed(LEFT)) {
                screenState = new TitleState();
            }
        }

        @Override
        public void update(float delta_time) {
            timer -= delta_time;
            if (timer < 0) {
                screenState = new TitleState();
                timer = 3;
            }
        }

        @Override
        public void display(PGraphics g) {

        }
    }

    class TitleState implements ScreenState {
        @Override
        public void handleInput() {
            if (NeonPulse.sInputState.isButtonPressed(LEFT)
                    || NeonPulse.sInputState.isKeyPressed(' ')
                    || NeonPulse.sInputState.isKeyPressed(PApplet.ENTER)) {
                screenState = new MapState();
            }
        }

        @Override
        public void update(float delta_time) {

        }

        @Override
        public void display(PGraphics g) {
            canvas.text("Neon Pulse", applet.width / 2, applet.height / 5);

            g.pushStyle();

            g.fill(127, 0 ,0, 127);
            g.stroke(0);

            for (PlayerButton button : playerButtons) {
                g.rect(button.x, button.y, button.w , button.h);
                Draw.player(g, button.x + button.w / 2, button.y + button.h / 2, button.player.angle, 48, button.player.fill);
            }

            g.popStyle();
        }
    }

    class MapState implements ScreenState {
        int selectedMap = 0;

        @Override
        public void handleInput() {
            PVector m = NeonPulse.sInputState.getMousePosition();
            if (NeonPulse.sInputState.isButtonPressed(LEFT)) {
                for (MapButton map : mapButtons) {
                    if (m.x >= map.x
                            && m.x <= map.x + BUTTON_WIDTH
                            && m.y >= map.y
                            && m.y <= map.y + BUTTON_HEIGHT) {
                        NeonPulse.goToScreen(mainScreen);
                        mainScreen.loadMap(map.index);
                        mainScreen.loadPlayers();
                        return;
                    }
                }
            }
            if (NeonPulse.sInputState.isKeyPressed('A'))
                selectedMap = (selectedMap == 0) ? mapButtons.size() - 1 : selectedMap - 1;
            if (NeonPulse.sInputState.isKeyPressed('D'))
                selectedMap = (selectedMap + 1) % mapButtons.size();

            if (NeonPulse.sInputState.isKeyPressed(' ')
                    || NeonPulse.sInputState.isKeyPressed(PApplet.ENTER)) {
                NeonPulse.goToScreen(mainScreen);
                mainScreen.loadMap(mapButtons.get(selectedMap).index);
                mainScreen.loadPlayers();
            }
        }

        @Override
        public void update(float delta_time) {
        }

        @Override
        public void display(PGraphics g) {
            canvas.text("Select Map", canvas.width / 2, canvas.height / 5);

            for (int i = 0; i < mapButtons.size(); i++) {
                MapButton map = mapButtons.get(i);
                canvas.image(map.image, map.x, map.y, map.w, map.h);
                if (i == selectedMap) {
                    canvas.pushStyle();
                    canvas.strokeWeight(8);
                    canvas.noFill();
                    canvas.rect(map.x - PADDING, map.y - PADDING, map.w + 2 * PADDING, map.h + 2 * PADDING);
                    canvas.popStyle();
                }
            }
        }
    }

    TitleScreen(PApplet applet, MainScreen game_screen) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);

        // Background
        background = applet.loadImage("art/bg3.jpg");
        background.resize(applet.width, applet.height);

        // Text
        rubber = applet.createFont("fonts/rubber.ttf", 144);
        pulsePoint = new PVector(0, 0);

        this.mainScreen = game_screen;

        // Level Buttons
        map1 = applet.loadImage("map1.png");
        map2 = applet.loadImage("map2.png");
        map3 = applet.loadImage("map3.png");

        mapButtons = new ArrayList<>();
        mapButtons.add(new MapButton("map1.tmx", 100, 360, BUTTON_WIDTH, BUTTON_HEIGHT, map1, 0));
        mapButtons.add(new MapButton("map2.tmx", 700, 360, BUTTON_WIDTH, BUTTON_HEIGHT, map2, 1));
        mapButtons.add(new MapButton("map3.tmx", 1300, 360, BUTTON_WIDTH, BUTTON_HEIGHT, map3, 2));

        playerButtons = new ArrayList<>();
    }

    public void load() {
        screenState = new IntroState();
        timer = 5.45f;

        players.clear();

        // Load Player 1 - Keyboard Control
        if (NeonPulse.Config.KEYBOARD) {
            addPlayer(new Player(applet, new KeyboardInput(NeonPulse.sInputState), NeonPulse.Debug.testSound, null));
        }

        // Load Controller Players
        List<ControlDevice> devices = NeonPulse.sControlIO.getDevices();
        for (ControlDevice gamepad : devices) {
            for (Configuration configuration : NeonPulse.sControllerConfigs) {
                if (gamepad.matches(configuration)) {
                    addPlayer(new Player(applet, new GamepadInput(gamepad), NeonPulse.Debug.testSound, null));
                    break;
                }
            }
        }

        int playersSize = players.size();
        int unit = applet.width / playersSize;
        int min_unit = applet.width / 5;
        for (int i = 0; i < playersSize; i++) {
            Player player = players.get(i);
            float x = i * unit + (unit / 2) - (min_unit / 2) + PADDING;
            float y = 360;
            float w = min_unit - 2 * PADDING;
            float h = 360;
            playerButtons.add(new PlayerButton(x, y, w, h, player));
        }
    }

    public void addPlayer(Player player) {
        player.setFill(Player.PLAYER_COLORS[players.size() % Player.PLAYER_COLORS.length]);
        super.addPlayer(player);
    }

    @Override
    public void handleInput() {
        screenState.handleInput();
    }

    public void update(float delta_time) {
        pulsePoint.x = (pulsePoint.x + delta_time * PULSE_SPEED) % applet.width;
        screenState.update(delta_time);
    }

    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0xffffffff);

        // Background
        canvas.image(background, 0, 0);

        // Screen
        canvas.pushStyle();
        canvas.textAlign(CENTER);
        canvas.strokeWeight(4);
        canvas.stroke(0xffffffff);
        canvas.textFont(rubber);
        canvas.point(pulsePoint.x, applet.height / 5 + 16);

        // Display
        screenState.display(canvas);

        canvas.popStyle();
        canvas.endDraw();

        return canvas;
    }

    public void unload() {
        // Finish
    }
}