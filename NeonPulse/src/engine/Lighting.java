package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Collections;

import static processing.core.PApplet.*;

public class Lighting {
    private final PGraphics lighting;
    private final ArrayList<Light> lights;
    private final PImage lightTexture;
    public boolean active;

    public Lighting(PApplet applet, PImage image) {
        this.lights = new ArrayList<>();
        this.lighting = applet.createGraphics(applet.width, applet.height, P2D);
        lightTexture = image;
        active = true;
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public void addLights(Light[] new_lights) {
        Collections.addAll(lights, new_lights);
    }

    public void removeLight(Light light) {
        lights.remove(light);
    }

    public void update(float delta_time) {
        for (Light l : lights) {
            l.update(delta_time);
        }
    }

    public void display(PGraphics g) {
        if (active) {
            lighting.beginDraw();
            lighting.fill(0);
            lighting.stroke(0);
            lighting.background(0, 127);
            lighting.imageMode(CENTER);
            for (Light l : lights) {
                l.display(lighting, lightTexture);
            }
            lighting.endDraw();

            g.blendMode(MULTIPLY);
            g.image(lighting, 0, 0);
            g.blendMode(BLEND);
        }
    }

    public void clear() {
        lights.clear();
    }
}