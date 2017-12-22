package skel;

import engine.Screen;
import processing.core.PApplet;
import processing.core.PGraphics;

public class BaseScreen extends Screen {

    protected BaseScreen(PApplet applet) {
        super(applet);
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
