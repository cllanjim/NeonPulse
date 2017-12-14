package util;

// A class to describe a group of Particles
// An ArrayList is used to manage the list of Particles

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class ParticleSystem
{
    ArrayList<Particle> particles;
    PVector origin;
    PApplet applet;

    public ParticleSystem(PApplet applet, PVector position)
    {
        this.applet=applet;
        origin = position.copy();
        particles = new ArrayList<Particle>();
    }

    public void emitParticle()
    {
        particles.add(new Particle(applet, origin));
    }

    public void attractParticle()
    {
        particles.add(new CenterParticle(applet, origin));
    }

    public void update()
    {
        for (int i = particles.size()-1; i >= 0; i--)
        {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead())
            {
                particles.remove(i);
            }
        }
    }//update end

    public void display(PGraphics g)
    {
        for (int i = particles.size()-1; i >= 0; i--)
        {

            Particle p = particles.get(i);
            p.display(g);
        }
    }


}
