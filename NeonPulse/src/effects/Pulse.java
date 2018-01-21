package effects;

import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

public class Pulse extends Effect {
    private final SoundFile sound;
    private final PVector velocity;
    private float outerRadius;
    private float innerRadius;
    private float cooldown;
    private static final float LIFESPAN = 0.2f;
    private static final float RADIUS = 128;
    private static final float FORCE = 64;
    private static final float COOLDOWN = 1;

    public Pulse(SoundFile pulse_sound) {
        this.velocity = new PVector(0, 0);
        sound = pulse_sound;
        outerRadius = 0;
        innerRadius = 0;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (live) {
            boolean colliding_inner = Collision.circular(agent.position, agent.radius, position, RADIUS / 2);
            boolean colliding_outer = Collision.circular(agent.position, agent.radius, position, RADIUS);
            if (colliding_outer) {
                agent.addImpulse(position, FORCE);
            }
            if (colliding_inner) {
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

    public void activate(PVector source, PVector velocity) {
        if (cooldown <= 0) {
            this.position.set(source);
            this.velocity.set(velocity);
            sound.play();
            lifetime = 0;
            active = true;
            live = true;
            cooldown = COOLDOWN;
        }
    }

    public void update(float delta_time) {
        if (active) {
            if (lifetime > 0) live = false;
            if (lifetime > LIFESPAN) active = false;
            position.add(PVector.mult(velocity, delta_time));
            innerRadius = PApplet.map(lifetime, 0, LIFESPAN, 0, RADIUS);
            outerRadius = PApplet.map(lifetime, 0, LIFESPAN, RADIUS / 2, RADIUS);
            lifetime += delta_time;
        }
        cooldown -= delta_time;
    }

    public boolean collideWithPayload(Payload payload) {
        if (active) {
            boolean colliding_outer = Collision.circular(payload.position, payload.radius, position, outerRadius * 1.0f);
            boolean colliding_inner = Collision.circular(payload.position, payload.radius, position, innerRadius * 0.5f);
            if (colliding_outer) {
                payload.setAcceleration(0, 0);
                payload.addImpulse(position, FORCE);
            }
            if (colliding_inner) {
                payload.addImpulse(position, FORCE);
            }
            return (colliding_inner || colliding_outer);
        }
        return false;
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            if (live) {
                g.fill(255, 63);
                g.ellipse(position.x, position.y, RADIUS * 2, RADIUS * 2);
            }
            g.noFill();
            g.strokeWeight(2);
            g.stroke(0xff00ff00, 127);
            g.ellipse(position.x, position.y, outerRadius * 2, outerRadius * 2);
            g.strokeWeight(1);
            g.ellipse(position.x, position.y, innerRadius * 2, innerRadius * 2);
            g.popStyle();
        }
    }
}
