import engine.Screen;
import util.Terrain;
import processing.core.*;

import static processing.core.PConstants.*;

class TitleScreen extends Screen {
    // Fonts
    private PFont rubber;
    private PGraphics canvas;
    private PImage background;

    Terrain mountains;
    Terrain floor;
    Sun sun;

    private PVector pulse_point;

    private static final float PULSE_SPEED = 1500;

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

    TitleScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height, P2D);

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
        pulse_point = new PVector(0, 0);
    }

    public void load() {
    }

    public void update(float deltatime) {
        pulse_point.x = (pulse_point.x + deltatime * PULSE_SPEED) % applet.width;
//         mountains.update();
//         floor.update();
//         sun.update();
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
        canvas.text("Neon Pulse", applet.width / 2, applet.height / 5);
        canvas.point(pulse_point.x, applet.height / 5 + 16);
        canvas.popStyle();
        canvas.endDraw();

        return canvas;
    }

    public void unload() {
        // Finish
    }
}