package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PApplet.map;
import static processing.core.PConstants.TWO_PI;

class Particle
{
    PVector position;
    PVector velocity;
    float angle;
    float lifetime;
    float lifespan;

    Particle(PApplet applet, PVector l, float life_span) {
        this(applet, l, life_span, 0, TWO_PI);
    }

    Particle(PApplet applet, PVector l, float life_span, float a1, float a2)
    {
        this.angle = applet.random(a1, a2);
        velocity = PVector.fromAngle(this.angle).setMag(applet.random(0, 2));
        position = l.copy();
        this.lifespan = life_span;
        this.lifetime = life_span;
    }

    Particle(PVector origin, float life_span, PVector velocity) {
        this.angle = velocity.heading();
        this.velocity = velocity.copy();
        this.position = origin.copy();
        this.lifespan = life_span;
        this.lifetime = life_span;
    }

    Particle(PVector l, float life_span, float angle, float speed)
    {
        this.angle = angle;
        this.velocity = PVector.fromAngle(this.angle).setMag(speed);
        this.position = l.copy();
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