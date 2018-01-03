package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PConstants.TWO_PI;

public class ParticleSystem {
    private final PApplet applet;
    private final ArrayList<Particle> particles;
    private final PVector origin;

    private float lifespan;
    private float minSpeed;
    private float maxSpeed;
    private float angle1;
    private float angle2;

    public ParticleSystem(PApplet applet, PVector origin, float life_span) {
        this.applet = applet;
        this.origin = origin.copy();
        this.lifespan = life_span;
        this.particles = new ArrayList<Particle>();
        this.angle1 = 0;
        this.angle2 = TWO_PI;
        this.minSpeed = 0;
        this.maxSpeed = 64;
    }

    public void setAngle(float angle, float spread) {
        angle1 = angle - spread / 2;
        angle2 = angle + spread / 2;
    }

    public void setSpeed(float min, float max) {
        minSpeed = min;
        maxSpeed = max;
    }

    public void setLifespan(float duration) {
        lifespan = duration;
    }

    private float getAngle() {
        return applet.random(angle1, angle2);
    }

    private float getSpeed() {
        return applet.random(minSpeed, maxSpeed);
    }

    public void emit() {
        particles.add(new Particle(origin, lifespan, getAngle(), getSpeed()));
    }

    public void attract() {
        particles.add(new CenterParticle(origin, lifespan, getAngle(), getSpeed()));
    }

    public void explode(int num_particles) {
        for (int a = 0; a < num_particles; a++) {
            particles.add(new Particle(origin, lifespan, getAngle(), getSpeed()));
        }
    }

    public void explode(int num_particles, PVector direction, float spread) {
        float targetAngle = direction.heading();
        float angle = applet.random(targetAngle - spread / 2, targetAngle + spread / 2);
        for (int a = 0; a < num_particles; a++) {
            float speed = applet.random(minSpeed, maxSpeed);
            particles.add(new Particle(origin, lifespan, angle, getSpeed()));
        }
    }

    public void implode(int num_particles) {
        for (int a = 0; a < num_particles; a++) {
            particles.add(new CenterParticle(origin, lifespan, getAngle(), getSpeed()));
        }
    }

    public void implode(int num_particles, PVector direction, float spread) {
        float targetAngle = direction.heading();
        for (int a = 0; a < num_particles; a++) {
            float angle = applet.random(targetAngle - spread / 2, targetAngle + spread / 2);
            particles.add(new CenterParticle(origin, lifespan, angle, getSpeed()));
        }
    }

    public void update(float delta_time) {
        for (int i = particles.size()-1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta_time);
            if (p.finished()) {
                particles.remove(i);
            }
        }
    }

    public void update(float delta_time, float x, float y) {
        origin.set(x, y);
        update(delta_time);
    }

    public void display(PGraphics g) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.display(g);
        }
    }
}