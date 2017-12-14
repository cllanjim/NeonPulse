package util;

import processing.core.PApplet;
import processing.core.PVector;

class CenterParticle extends Particle
{
    float pX, pY;
    float centerX, centerY;

    CenterParticle(PApplet applet, PVector l)
    {
        super(applet, l);
        pX = applet.random(-100, 100);//size of possible distance from center
        pY = applet.random(-100, 100);

        centerX = l.x;//just used for center circle for testing purposes
        centerY = l.y;

        position = new PVector (l.x+pX, l.y+pY);//creates particle in a random distance from the received point 'l'
        velocity = new PVector((l.x - position.x)*0.05f, (l.y - position.y)*0.05f);//direction it goes from original point received

        lifespan = 255.0f;
    }

    // Method to update position
    void update()
    {
        position.add(velocity);
        lifespan -= 5.0;//change how long particle is live

        if(position.x>=centerX-5 && position.x<=centerX+5)
            lifespan = 0;
    }
}