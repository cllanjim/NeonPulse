package util;

import processing.core.PApplet;
import processing.core.PVector;

import static processing.core.PConstants.TWO_PI;

class CenterParticle extends Particle
{
    float centerX, centerY;
    PVector pV;

    CenterParticle(PApplet applet, PVector l, float life_span)
    {
        super(applet, l, life_span);

        this.angle = applet.random(0, TWO_PI);
        pV = PVector.fromAngle(angle).setMag(applet.random(0,50));

        centerX = l.x;//area where particles disappear
        centerY = l.y;

        position = new PVector (l.x+pV.x, l.y+pV.y);//creates particle in a random distance from the received point 'l'
        velocity = new PVector(((l.x - position.x)), ((l.y - position.y)));
    }

    CenterParticle(PApplet applet, PVector l, float life_span, float a1, float a2)
    {
        super(applet, l, life_span);

        this.angle = applet.random(a1, a2);

        pV = PVector.fromAngle(angle).setMag(applet.random(0,100));

        centerX = l.x;//area where particles disappear
        centerY = l.y;

        position = new PVector (l.x+pV.x, l.y+pV.y);//creates particle in a random distance from the received point 'l'
        velocity = new PVector(((l.x - position.x)*0.5f), ((l.y - position.y)*0.5f));
    }

    // Method to update position
    void update(float delta_time)
    {
        super.update(delta_time);
        if(position.x>=centerX-5 && position.x<=centerX+5)
            lifespan = 0;
    }
}