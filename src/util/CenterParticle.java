package util;

import processing.core.PApplet;
import processing.core.PVector;

class CenterParticle extends Particle
{
    float pX, pY;
    float centerX, centerY;
    PVector pV;

    CenterParticle(PApplet applet, PVector l)
    {
        super(applet, l);

        pV= PVector.fromAngle(angle).setMag(applet.random(0,100));

        centerX = l.x;//area where particles disappear
        centerY = l.y;

        position = new PVector (l.x+pV.x, l.y+pV.y);//creates particle in a random distance from the received point 'l'
        velocity = new PVector(((l.x - position.x)*0.05f), ((l.y - position.y)*0.05f));

        lifespan = 255.0f;
    }

    CenterParticle(PApplet applet, PVector l, float a1, float a2)
    {
        super(applet, l);

        this.angle=applet.random(a1, a2);
        pV= PVector.fromAngle(angle).setMag(applet.random(0,100));

        centerX = l.x;//area where particles disappear
        centerY = l.y;

        position = new PVector (l.x+pV.x, l.y+pV.y);//creates particle in a random distance from the received point 'l'
        velocity = new PVector(((l.x - position.x)*0.05f), ((l.y - position.y)*0.05f));

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