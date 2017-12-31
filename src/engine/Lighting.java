package engine;

import processing.core.*;
import static processing.core.PApplet.*;

public class Lighting {
    private PGraphics lighting;
    private PImage light;
    private PVector position;
    private int radius;

    Lighting(PApplet applet, PVector position, int radius) {
        this.position = position.copy();
        this.radius = radius;
        light = applet.createImage(radius * 2, radius * 2, ARGB);
        light.loadPixels();
        for (int line = 0; line != light.height; line++)
            for (int column = 0; column != light.width; column++) {
                int pixel = line * light.width + column;
                float distance = dist(column, line, light.width / 2, light.height / 2);
                float factor = exp(-sqr(distance / radius));
                light.pixels[pixel] = applet.color(factor * 255);
            }
        light.updatePixels();
    }

    Lighting(PApplet applet, PVector position, int radius, PImage image) {
        this.position = position.copy();
        this.radius = radius;
        light = image;
    }

    private static float sqr(float x) {
        return x * x;
    }

    void update(float delta_time) {

    }

    void display(PGraphics g) {
        g.pushStyle();
        g.rectMode(CORNER);
        g.fill(0);
        g.stroke(0);
        if (position.y - radius > 0)
            g.rect(0, 0, g.width, position.y - radius);
        if (position.y + radius < g.height)
            g.rect(0, position.y + radius, g.width, g.height - position.y - radius);
        if (position.x - radius > 0)
            g.rect(0, 0, position.x - radius, g.height);
        if (position.x + radius < g.width)
            g.rect(position.x + radius, 0, g.width - position.x - radius, g.height);
        g.blendMode(MULTIPLY);
        g.imageMode(CENTER);
        g.image(light, position.x, position.y, 2 * radius, 2 * radius);
        g.popStyle();
    }
}