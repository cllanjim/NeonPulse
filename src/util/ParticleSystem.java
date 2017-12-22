package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class ParticleSystem
{
    private final ArrayList<Particle> particles;
    private final PVector origin;
    private final PApplet applet;
    private final float lifespan;

    public ParticleSystem(PApplet applet, PVector position, float life_span)
    {
        this.applet = applet;
        origin = position.copy();
        particles = new ArrayList<Particle>();
        lifespan = life_span;
    }

    public void emitParticle()
    {
        particles.add(new Particle(applet, origin, lifespan));
    }

    public void emitParticle(float angle1, float angle2) {
        float angle = applet.random(angle1, angle2);
        float speed = applet.random(0, 2);
        particles.add(new Particle(origin, lifespan, angle, speed));
    }

    public void attractParticle()
    {
        particles.add(new CenterParticle(applet, origin, lifespan));
    }

    public void attractParticleAngle(float angle1, float angle2)
    {
        particles.add(new CenterParticle(applet, origin, lifespan, angle1, angle2));
    }

    public void explodeParticle(int n)
    {
        for (int a = 0; a < n; a++)
        {
            particles.add(new Particle(applet, origin, lifespan));
        }
    }

    public void explodeParticle(int n, float angle1, float angle2)
    {
        for (int a = 0; a < n; a++)
        {
            particles.add(new Particle(applet, origin, lifespan, angle1, angle2));
        }
    }

    public void explodeParticleAngle(int n, float x, float y, float spread)
    {
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        for (int a = 0; a < n; a++)
        {
            particles.add(new Particle(applet, origin, lifespan, targetAngle - spread / 2, targetAngle + spread / 2));
        }
    }

    public void implodeParticle(int n)
    {
        for (int a = 0; a < n; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan));
        }
    }

    public void implodeParticleAngle(int n, float angle1, float angle2)
    {
        for (int a = 0; a < n; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan, angle1, angle2));
        }
    }

    public void implodeParticleAngle(int n, float x, float y, float spread)
    {
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        for (int a = 0; a < n; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan, targetAngle - spread/2, targetAngle + spread / 2));
        }
    }

    public void update(float delta_time)
    {
        for (int i = particles.size()-1; i >= 0; i--)
        {
            Particle p = particles.get(i);
            p.update(delta_time);
            if (p.finished())
            {
                particles.remove(i);
            }
        }
    }

    public void update(float delta_time, float x, float y)
    {
        origin.set(x, y);
        update(delta_time);
    }

    public void display(PGraphics g)
    {
        for (int i = particles.size()-1; i >= 0; i--)
        {
            Particle p = particles.get(i);
            p.display(g);
        }
    }
}
