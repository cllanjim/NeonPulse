package util;

import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PApplet.map;

class Particle
{
    PVector position;
    PVector velocity;
    private float lifetime;
    float lifespan;

    Particle(PVector origin, float life_span, PVector velocity) {
        this.velocity = velocity.copy();
        this.position = origin.copy();
        this.lifespan = life_span;
        this.lifetime = life_span;
    }

    Particle(PVector origin, float life_span, float angle, float speed)
    {
        this.velocity = PVector.fromAngle(angle).setMag(speed);
        this.position = origin.copy();
        this.lifespan = life_span;
        this.lifetime = life_span;
    }

    void update(float delta_time)
    {
        position.add(PVector.mult(velocity, delta_time));
        lifespan -= delta_time;
    }

    void display(PGraphics g)
    {
        float opacity = map(lifespan, 0, lifetime, 0, 255);

        g.pushStyle();
        g.stroke(255, 0, 0, opacity);
        g.fill(255, opacity);
        g.ellipse(position.x, position.y, 4, 4);
        g.popStyle();
    }

    boolean finished()
    {
        return lifespan < 0;
    }
}