package engine;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import static processing.core.PApplet.sin;

public class Light {
    private final PVector position;
    private float lifetime;
    private float radius;

    private static final float RATE = 1;
    private static final float INTENSITY = 40;

    public Light(float x, float y, float radius) {
        this.position = new PVector(x, y);
        this.lifetime = 0;
        this.radius = radius;
    }

    public void updatePosition(float x, float y) {
        this.position.set(x, y);
    }

    public void update(float delta_time) {
        lifetime += delta_time;
    }

    void display(PGraphics g, PImage lightTexture) {
        float width = radius * 2 + sin(lifetime * RATE) * INTENSITY;
        g.image(lightTexture, position.x, position.y, width, width);
    }
}
