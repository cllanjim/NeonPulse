package effects;

import processing.sound.SoundFile;
import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Pulse extends Effect {
    private float outer_radius;
    private float inner_radius;

    private static final float LIFESPAN = 0.2f;
    private static final float RADIUS = 64;
    private static final float FORCE = 128;

    public Pulse(SoundFile pulse_sound) {
        super(pulse_sound);
        outer_radius = 0;
        inner_radius = 0;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            boolean colliding_inner = Collision.circular(agent.position, agent.radius, position, inner_radius * 0.2f);
            boolean colliding_outer = Collision.circular(agent.position, agent.radius, position, outer_radius * 1.1f);
            if (colliding_inner) {
                agent.addImpulse(position, FORCE * 2);
            }
            if (colliding_outer) {
                agent.addImpulse(position, FORCE);
            }
            return (colliding_inner || colliding_outer);
        }
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    public void activate(PVector source, PVector target) {
        if (cooldown <= 0) {
            this.position.set(source);
            sound.play();
            lifetime = 0;
            active = true;
            cooldown = COOLDOWN;
        }
    }

    public void update(float delta_time) {
        if (active) {
            lifetime += delta_time;
            outer_radius = PApplet.map(lifetime, 0, LIFESPAN, RADIUS - 10, RADIUS);
            inner_radius = PApplet.map(lifetime, 0, LIFESPAN, 0, RADIUS - 2);
            if (lifetime > LIFESPAN) {
                active = false;
            }
        }

        cooldown -= delta_time;
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.noFill();
            g.strokeWeight(2);
            g.stroke(0xff00ff00, 127);
            g.ellipse(position.x, position.y, outer_radius * 2, outer_radius * 2);
            g.strokeWeight(1);
            g.ellipse(position.x, position.y, inner_radius * 2, inner_radius * 2);
            g.popStyle();
        }
    }
}
