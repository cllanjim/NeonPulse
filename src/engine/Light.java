package engine;

import processing.core.PVector;

public class Light {
    PVector position;
    float radius;

    public Light(float x, float y, float radius) {
        this.position = new PVector(x, y);
        this.radius = radius;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void setRadius(float r) {
        this.radius = r;
    }

    public void update(float delta_time) {

    }
}
