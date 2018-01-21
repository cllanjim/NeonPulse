package effects;

import engine.*;
import game.Draw;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class Payload extends Effect {
    private final Light light;
    private final Lighting lighting;
    private final Effect effect;
    private final PVector[] history;
    public final PVector velocity;
    private final PVector acceleration;
    public float radius;
    private float mass;
    private float angle;
    private int frameCount;

    public ParticleSystem particleSystem, particleSystem2, particleSystem3;

    public static final float DELAY = 0.5f;
    private static final int TAIL_LENGTH = 8;
    private static final int FORCE = 64;

    public Payload(Effect payload_effect, PApplet applet, Lighting lighting) {
        particleSystem = new ParticleSystem(applet, position, 0.2f, 560, 560);
        particleSystem2 = new ParticleSystem(applet, position, 0.2f, 280, 280);
        particleSystem3 = new ParticleSystem(applet, position, 0.2f, 140, 140);
        effect = payload_effect;
        velocity = new PVector(0, 0);
        acceleration = new PVector(0, 0);
        history = new PVector[TAIL_LENGTH];
        frameCount = 0;
        radius = 8;
        mass = 1;
        angle = 0;
        light = new Light(position.x, position.y, 100);
        this.lighting = lighting;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (Collision.circular(position, radius, agent.position, agent.radius)) {
                activateEffect(agent.position);
                agent.addImpulse(position, FORCE);
                agent.damageLethal(100);
                return true;
            }
        }
        return effect.collideWithAgent(agent);
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
    public void activate(PVector source, PVector velocity) {
        position.set(source);
        this.velocity.set(velocity);
        active = true;
        lifetime = 0;
        frameCount = 0;
        lighting.addLight(light);
    }

    public void activateEffect(PVector target) {
        if (active) {
            active = false;
            effect.activate(position, target);
            lighting.removeLight(light);
            particleSystem.explode(200);
            particleSystem2.explode(100);
            particleSystem3.explode(100);
        }
    }

    public void updateMovement(float delta_time) {
        if (active) {
            velocity.add(PVector.mult(acceleration, delta_time));
            position.add(PVector.mult(velocity, delta_time));
            angle = velocity.heading();
        }
    }

    @Override
    public void update(float delta_time) {
        if (active) {
            light.updatePosition(position.x + 64, position.y + 60);
            history[frameCount % TAIL_LENGTH] = position.copy();
            if (lifetime > DELAY) {
                activateEffect(position);
            }
            lifetime += delta_time;
            frameCount += 1;
        }

        particleSystem.update(delta_time, position.x+velocity.x*delta_time, position.y+velocity.y*delta_time);
        particleSystem2.update(delta_time, position.x+velocity.x*delta_time, position.y+velocity.y*delta_time);
        particleSystem3.update(delta_time, position.x+velocity.x*delta_time, position.y+velocity.y*delta_time);

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
            for (int i = 1; i < history.length && i < frameCount; i++) {
                Draw.polygon(g, history[i].x, history[i].y, radius, 3, angle);
            }
            g.popStyle();
        }
        if (effect.active) {
            effect.display(g);
            particleSystem.display(g);
            particleSystem2.display(g);
            particleSystem3.display(g);
        }
    }

    public void setAcceleration(float x, float y) {
        acceleration.set(x, y);
    }

    public void addImpulse(PVector impulse, float v) {
        velocity.add(impulse. normalize().mult(v / mass));
    }
}
