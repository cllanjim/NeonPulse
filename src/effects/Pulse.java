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
    private float outerRadius;
    private float innerRadius;
    private static final float LIFESPAN = 0.2f;
    private static final float RADIUS = 128;
    private static final float FORCE = 128;

    public Pulse(SoundFile pulse_sound) {
        sound = pulse_sound;
        outerRadius = 0;
        innerRadius = 0;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            boolean colliding_inner = Collision.circular(agent.position, agent.radius, position, innerRadius * 0.2f);
            boolean colliding_outer = Collision.circular(agent.position, agent.radius, position, outerRadius * 1.1f);
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
            this.position.set(source);
            sound.play();
            lifetime = 0;
            active = true;
    }

    public void update(float delta_time) {
        if (active) {
            lifetime += delta_time;
            outerRadius = PApplet.map(lifetime, 0, LIFESPAN, RADIUS - 10, RADIUS);
            innerRadius = PApplet.map(lifetime, 0, LIFESPAN, 0, RADIUS - 2);
            if (lifetime > LIFESPAN) {
                active = false;
            }
        }
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
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
