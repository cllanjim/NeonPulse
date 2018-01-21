package effects;

import engine.Agent;
import engine.Tile;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.map;
import static processing.core.PConstants.TWO_PI;

public class ParticleSystem extends Effect {
    private final PApplet applet;
    private final ArrayList<Particle> particles;
    private final PVector origin;

    private float lifespan;
    private float minSpeed;
    private float maxSpeed;
    private float angle1;
    private float angle2;

    public ParticleSystem(PApplet applet, PVector origin, float life_span, float minSpeed, float maxSpeed) {
        this.applet = applet;
        this.origin = origin.copy();
        this.lifespan = life_span;
        this.particles = new ArrayList<Particle>();
        this.angle1 = 0;
        this.angle2 = TWO_PI;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
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

    public void setOrigin(PVector position) {
        origin.set(position);
    }

    private float getRandomAngle() {
        return applet.random(angle1, angle2);
    }

    private float getRandomSpeed() {
        return applet.random(minSpeed, maxSpeed);
    }

    public void emit() {
        particles.add(new Particle(origin, lifespan, getRandomAngle(), getRandomSpeed()));
    }

    public void attract() {
        particles.add(new CenterParticle(origin, lifespan, getRandomAngle(), getRandomSpeed()));
    }

    public void explode(int num_particles) {
        for (int a = 0; a < num_particles; a++) {
            particles.add(new Particle(origin, lifespan, getRandomAngle(), getRandomSpeed()));
        }
    }

    public void explode(int num_particles, PVector direction, float spread) {
        float targetAngle = direction.heading();
        float angle = applet.random(targetAngle - spread / 2, targetAngle + spread / 2);
        for (int a = 0; a < num_particles; a++) {
            float speed = applet.random(minSpeed, maxSpeed);
            particles.add(new Particle(origin, lifespan, angle, getRandomSpeed()));
        }
    }

    public void implode(int num_particles) {
        for (int a = 0; a < num_particles; a++) {
            particles.add(new CenterParticle(origin, lifespan, getRandomAngle(), getRandomSpeed()));
        }
    }

    public void implode(int num_particles, PVector direction, float spread) {
        float targetAngle = direction.heading();
        for (int a = 0; a < num_particles; a++) {
            float angle = applet.random(targetAngle - spread / 2, targetAngle + spread / 2);
            particles.add(new CenterParticle(origin, lifespan, angle, getRandomSpeed()));
        }
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    @Override
    public void activate(PVector source, PVector target) {

    }

    @Override
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

    @Override
    public void display(PGraphics g) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.display(g);
        }
    }

    static class Particle {
        PVector position;
        float lifetime;
        PVector velocity;

        private final float LIFETIME;

        Particle(PVector origin, float life_span, float angle, float speed) {
            this.velocity = PVector.fromAngle(angle).setMag(speed);
            this.position = origin.copy();
            this.lifetime = life_span;
            this.LIFETIME = life_span;
        }

        public void update(float delta_time) {
            position.add(PVector.mult(velocity, delta_time));
            lifetime -= delta_time;
        }

        public void display(PGraphics g) {
            // float opacity = map(lifetime, 0, LIFETIME, 0, 255);

            g.pushStyle();
            // g.fill(255, opacity);
            g.ellipse(position.x, position.y, 4, 4);
            g.popStyle();
        }

        boolean finished() {
            return lifetime <= 0;
        }
    }

    static class CenterParticle extends Particle {
        private final PVector center;

        private final float SPEED;

        CenterParticle(PVector origin, float life_span, float angle, float speed) {
            super(origin, life_span, angle, speed);
            PVector offset = PVector.fromAngle(angle).setMag(speed);
            SPEED = speed;
            this.center = origin.copy();
            this.position.set(origin.x + offset.x, origin.y + offset.y);
            this.velocity.set( (origin.x - position.x),(origin.y - position.y));
        }

        public void display(PGraphics g) {
            float opacity = map(PVector.dist(position, center), 0, SPEED, 0, 255);

            g.pushStyle();
            g.fill(255, opacity);
            g.ellipse(position.x, position.y, 4, 4);
            g.popStyle();
        }

        public void update(float delta_time) {
            super.update(delta_time);
            velocity.mult(1.1f);
            if (PVector.dist(position, center) < 5)
                lifetime = 0;
        }
    }
}