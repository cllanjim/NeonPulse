package util;

import processing.core.*;

import java.util.ArrayList;

public class Particles {
    private PVector origin;
    private PImage texture;
    private ArrayList<Particle> particles;

    Particles(int num, PVector v, PImage img_) {
        particles = new ArrayList<>(num);
        origin = v.copy();
        texture = img_;
        for (int i = 0; i < num; i++) {
            particles.add(new Particle(origin, texture));
        }
    }

    Particles(int num, PVector v) {
        particles = new ArrayList<>(num);
        origin = v.copy();
        for (int i = 0; i < num; i++) {
            particles.add(new Particle(origin));
        }
    }

    public void update(float delta_time) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta_time);
            if (p.finished()) {
                particles.remove(i);
            }
        }
    }

    public void show(PGraphics g) {
        for (Particle p : particles) {
            p.show(g);
        }
    }

    public void applyForce(PVector dir) {
        for (Particle p : particles) {
            p.applyForce(dir);
        }
    }

    public void addParticles(int n) {
        for (int i = 0; i < n; i++) {
            particles.add(new Particle(origin, texture));
        }
    }

    static class Particle {
        PVector position;
        PVector velocity;
        PVector acceleration;
        float lifespan;
        float radius;
        PImage texture;

        Particle(PVector l) {
            acceleration = new PVector(0, 0.05f);
            velocity = new PVector(0, 0);
            position = l.copy();
            lifespan = 0.5f;
            radius = 8;
        }

        Particle(PVector l, PImage img) {
            this(l);
            texture = img;
        }

        void update(float delta_time) {
            velocity.add(acceleration);
            position.add(velocity.mult(delta_time));
            lifespan -= delta_time;
        }

        boolean finished() {
            return (lifespan < 0.0f);
        }

        void applyForce(PVector f) {
            acceleration.add(f);
        }

        void show(PGraphics g) {
            g.pushStyle();
            if (texture != null) {
                g.imageMode(PConstants.CENTER);
                g.tint(191, 0, 127, lifespan);
                g.image(texture, position.x, position.y);
                g.imageMode(PConstants.CORNER);
            } else {
                g.fill(191, 0, 127, lifespan);
                g.noStroke();
                g.ellipse(position.x, position.y, radius * 2, radius * 2);
            }
            g.popStyle();
        }
    }

    static class RotatingParticle extends Particle {
        float angular_velocity;
        float angle;

        RotatingParticle(PVector l) {
            super(l);
            angle = 0.0f;
            angular_velocity = 0.0f;
        }

        public void update(float delta_time) {
            super.update(delta_time);
            angle += angular_velocity;
        }

        public void show(PGraphics g) {
            g.pushMatrix();
            g.translate(position.x, position.y);
            g.rotate(angle);
            g.stroke(255, lifespan);
            g.ellipse(radius, radius, 4, 4);
            g.popMatrix();
        }
    }
}