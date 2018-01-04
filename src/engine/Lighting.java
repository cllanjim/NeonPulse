package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;

import static processing.core.PApplet.*;

public class Lighting {
    private PGraphics lighting;
    private ArrayList<Light> lights;
    private PImage lightTexture;

    private static float sqr(float x) {
        return x * x;
    }

    public Lighting(PApplet applet, int radius) {
        this.lights = new ArrayList<>();
        this.lighting = applet.createGraphics(applet.width, applet.height, P2D);
        lightTexture = applet.createImage(radius * 2, radius * 2, ARGB);
        lightTexture.loadPixels();
        for (int line = 0; line < lightTexture.height; line++)
            for (int column = 0; column < lightTexture.width; column++) {
                int pixel = line * lightTexture.width + column;
                float distance = dist(column, line, lightTexture.width / 2, lightTexture.height / 2);
                float factor = exp(-sqr(distance / radius));
                lightTexture.pixels[pixel] = applet.color(factor * 255);
            }
        lightTexture.updatePixels();
    }

    public Lighting(PApplet applet, PImage image) {
        this.lights = new ArrayList<>();
        this.lighting = applet.createGraphics(applet.width, applet.height, P2D);
        lightTexture = image;
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    void update(float delta_time) {

    }

    public void display(PGraphics g) {
        lighting.beginDraw();
        lighting.fill(0);
        lighting.stroke(0);
        lighting.clear();
        lighting.imageMode(CENTER);
        for (Light l : lights) {
            lighting.image(lightTexture, l.position.x, l.position.y, 2 * l.radius, 2 * l.radius);
        }
        lighting.endDraw();

        g.blendMode(MULTIPLY);
        g.image(lighting, 0, 0);
        g.blendMode(BLEND);
    }

    public void clear() {
        lights.clear();
    }
}