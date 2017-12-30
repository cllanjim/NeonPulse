package effects;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;
import engine.Agent;
import engine.Collision;
import engine.Tile;
import engine.Draw;

public class Payload extends Effect {
    private Effect effect;
    private PVector[] history;
    private PVector velocity;
    private PVector acceleration;
    private float angle;
    private int frame_count;
    public float radius;

    private static final float DELAY = 0.5f;
    private static final int TAIL_LENGTH = 8;
    private static final int FORCE = 128;

    public Payload(Effect payload_effect, SoundFile payload_sound) {
        super(payload_sound);
        effect = payload_effect;
        velocity = new PVector(0, 0);
        acceleration = new PVector(0, 0);
        history = new PVector[TAIL_LENGTH];
        frame_count = 0;
        radius = 8;
        angle = 0;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (Collision.circular(position, radius, agent.position, agent.radius)) {
                activateEffect(agent.position);
                agent.addImpulse(position, FORCE);
                agent.damageLethal(35);
                return true;
            }
        }
        effect.collideWithAgent(agent);
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        if (active) {
            if(tile.isSolid()) {
                activateEffect(position);
                return true;
            }
        }
        return false;
    }

    @Override
    public void activate(PVector source, PVector target) {
        position.set(source);
        active = true;
        lifetime = 0;
        frame_count = 0;
        velocity.set(target);
    }

    public void activateEffect(PVector target) {
        if (active) {
            active = false;
            effect.activate(position, target);
        }
    }

    @Override
    public void update(float delta_time) {
        if (active) {
            velocity.add(acceleration);
            position.add(PVector.mult(velocity, delta_time));
            angle = velocity.heading();
            history[frame_count % TAIL_LENGTH] = position.copy();
            if (lifetime > DELAY) {
                activateEffect(position);
            }
            lifetime += delta_time;
            frame_count += 1;
        }

        effect.update(delta_time);
    }

    @Override
    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.noStroke();
            g.fill(0xfff50455);
            Draw.polygon(g, position.x, position.y, radius, 3, angle + PConstants.PI);
            Draw.polygon(g, position.x, position.y, radius, 3, angle);
            for (int i = 1; i < history.length && i < frame_count; i++) {
                Draw.polygon(g, history[i].x, history[i].y, radius, 3, angle);
            }
            g.popStyle();
        }
        if (effect.active) {
            effect.display(g);
        }
    }

    public void setAcceleration(float x, float y) {
        acceleration.set(x, y);
    }
}
