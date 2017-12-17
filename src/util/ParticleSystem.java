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

    float angle1, angle2;

    public ParticleSystem(PApplet applet, PVector position)
    {
        this.applet=applet;
        origin = position.copy();
        particles = new ArrayList<Particle>();
    }

    public ParticleSystem(PApplet applet, PVector position, float a1, float a2)
    {
        angle1=a1;
        angle2=a2;
        this.applet=applet;
        origin = position.copy();
        particles = new ArrayList<Particle>();
    }

    public void emitParticle()
    {
        particles.add(new Particle(applet, origin));
    }

    public void emitParticleAngle()
    {
        particles.add(new Particle(applet, origin, angle1, angle2));
    }

    public void attractParticle()
    {
        particles.add(new CenterParticle(applet, origin));
    }

    public void attractParticleAngle()
    {
        particles.add(new CenterParticle(applet, origin, angle1, angle2));
    }

    public void explodeParticle()
    {
        for (int a=0; a<100; a++)
        {
            particles.add(new Particle(applet, origin));
        }
    }

    public void explodeParticleAngle()
    {
        for (int a=0; a<100; a++)
        {
            particles.add(new Particle(applet, origin, angle1, angle2));
        }
    }

    public void explodeParticleAngle(float x, float y)
    {
        float angularRange = angle2 - angle1;
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        for (int a=0; a<100; a++)
        {
            particles.add(new Particle(applet, origin, targetAngle - angularRange/2, targetAngle + angularRange / 2));
        }
    }

    public void implodeParticle()
    {
        for (int a=0; a<100; a++)
        {
            particles.add(new CenterParticle(applet, origin));
        }
    }

    public void implodeParticleAngle()
    {
        for (int a=0; a<100; a++)
        {
            particles.add(new CenterParticle(applet, origin, angle1, angle2));
        }
    }

    public void implodeParticleAngle(float x, float y)
    {
        //does not create particles at pi/2 and 3pi/2
        float angularRange = angle2 - angle1;
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        for (int a=0; a<100; a++)
        {
            particles.add(new CenterParticle(applet, origin, targetAngle - angularRange/2, targetAngle + angularRange / 2));
        }
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

    public void update(float x, float y)
    {
        origin.x=x;
        origin.y=y;
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
