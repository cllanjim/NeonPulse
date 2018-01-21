package engine;

import ch.bildspur.postfx.builder.PostFX;
import game.Player;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public abstract class GameScreen {
    protected final PApplet applet;
    protected static List<Player> players = new ArrayList<>(4);

    public abstract void load();
    public abstract void update(float deltatime);
    public abstract PGraphics render();
    public abstract void unload();

    protected GameScreen(PApplet applet) {
        this.applet = applet;
    }

    public void renderFX(PostFX fx) { }

    public void handleInput() { }
    public void addPlayer(Player player) {
        players.add(player);
    }

    public interface ScreenState {
        void handleInput();
        void update(float delta_time);
        void display(PGraphics g);
    }
}
