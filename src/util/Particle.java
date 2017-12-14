package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle
{
    PVector position;
    PVector velocity;
    PVector acceleration;
    float lifespan;

    Particle(PApplet applet, PVector l)
    {
        velocity = new PVector(applet.random(-2, 2), applet.random(-2, 2));//direction it goes from original point received
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