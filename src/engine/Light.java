package engine;

import processing.core.PVector;

public class Light {
    PVector position;
    float radius;

    public Light(float x, float y, float radius) {
        this.position = new PVector(x, y);
        this.radius = radius;
    }

    public void setPosition(PVector position) {
        this.position.set(position);
    }
}
