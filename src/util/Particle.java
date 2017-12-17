package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.javafx.PSurfaceFX;

class Particle
{
    PVector position;
    PVector velocity;
    float angle;
    float lifespan;

    Particle(PApplet applet, PVector l)
    {
        angle= applet.random(0, applet.TWO_PI);
        velocity= PVector.fromAngle(angle);
        velocity.x=velocity.x*applet.random(0, 2);
        velocity.y=velocity.y*applet.random(0, 2);

        position = l.copy();
        lifespan = 255.0f;
    }

    Particle(PApplet applet, PVector l, float a1, float a2)
    {
        angle= applet.random(a1, a2);
        velocity= PVector.fromAngle(angle);
        velocity.x=velocity.x*applet.random(0, 2);
        velocity.y=velocity.y*applet.random(0, 2);

        position = l.copy();
        lifespan = 255.0f;
    }



    // Method to update position
    void update()
    {
        position.add(velocity);
        lifespan -= 5.0;//change how long particle is live
    }

    // Method to display
    void display(PGraphics g)
    {
        g.stroke(255, 0, 0, lifespan);
        g.fill(255, lifespan);
        g.ellipse(position.x, position.y, 8, 8);
    }

    // Is the particle still useful?
    boolean isDead()
    {
        if (lifespan < 0.0)
        {
            return true;
        } else {
            return false;
        }
    }
}