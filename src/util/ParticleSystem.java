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

    public void emit()
    {
        particles.add(new Particle(applet, origin, lifespan));
    }

    public void emit(float angle1, float angle2) {
        float angle = applet.random(angle1, angle2);
        float speed = applet.random(0, 2);
        particles.add(new Particle(origin, lifespan, angle, speed));
    }

    public void attract()
    {
        particles.add(new CenterParticle(applet, origin, lifespan));
    }

    public void attract(float angle1, float angle2)
    {
        particles.add(new CenterParticle(applet, origin, lifespan, angle1, angle2));
    }

    public void explode(int num_particles)
    {
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new Particle(applet, origin, lifespan));
        }
    }

    public void explode(int num_particles, float angle1, float angle2)
    {
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new Particle(applet, origin, lifespan, angle1, angle2));
        }
    }

    public void explode(int num_particles, float x, float y, float spread)
    {
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        float angle1 = targetAngle - spread / 2;
        float angle2 = targetAngle + spread / 2;
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new Particle(applet, origin, lifespan, angle1, angle2));
        }
    }

    public void implode(int num_particles)
    {
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan));
        }
    }

    public void implode(int num_particles, float angle1, float angle2)
    {
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan, angle1, angle2));
        }
    }

    public void implode(int num_particles, float x, float y, float spread)
    {
        float targetAngle = PVector.sub(new PVector(x, y), origin).heading();
        float angle1 = targetAngle - spread / 2;
        float angle2 = targetAngle + spread / 2;
        for (int a = 0; a < num_particles; a++)
        {
            particles.add(new CenterParticle(applet, origin, lifespan, angle1, angle2));
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
