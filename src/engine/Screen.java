package engine;

import ch.bildspur.postfx.builder.PostFX;
import game.Player;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.net.Client;

import java.util.ArrayList;

public abstract class Screen {
    protected PApplet applet;
    protected PGraphics canvas;
    protected static ArrayList<Player> players = new ArrayList<>(4);

    public abstract void load();
    public abstract void update(float deltatime);
    public abstract PGraphics render();
    public abstract void unload();

    public void renderFX(PostFX fx) { }

    // Textures
    protected Screen(PApplet applet) {
        this.applet = applet;
    }

    public void handleInput() { }
    public void addPlayer(Player player) {
        players.add(player);
    }
}
