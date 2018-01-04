package skel;

import engine.GameScreen;
import processing.core.PApplet;
import processing.core.PGraphics;

public class BaseScreen extends GameScreen {

    private final PGraphics canvas;

    protected BaseScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height);
    }

    @Override
    public void load() {

    }

    @Override
    public void update(float deltatime) {

    }

    @Override
    public PGraphics render() {
        return null;
    }

    @Override
    public void unload() {

    }
}
