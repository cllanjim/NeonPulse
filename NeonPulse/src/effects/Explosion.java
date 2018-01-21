package effects;

import engine.*;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

public class Explosion extends Effect {
    private Light light;
    private Lighting lighting;
    private SoundFile sound;
    private float radius;

    private static final float LIFESPAN = 0.2f;
    private static final float FORCE = 64;

    public Explosion(SoundFile explosion_sound, Lighting lighting) {
        sound = explosion_sound;
        this.lighting = lighting;
        this.light = new Light( position.x, position.y, 200);
        radius = 128;
    }

    public void activate(PVector source, PVector target) {
        position.set(source);
        sound.play();
        active = true;
        live = true;
        lifetime = 0;
        lighting.addLight(light);
    }

    public void update(float deltatime) {
        if (active) {
            light.updatePosition(position.x + 64, position.y + 60);
            if (lifetime > 0) live = false;
            if (lifetime > LIFESPAN) {
                active = false;
                lighting.removeLight(light);
            }
            lifetime += deltatime;
        }
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.noStroke();
            g.fill(191, 0, 31);

            if (live) {
                g.stroke(0xffffffff);
                g.strokeWeight(2);
                g.ellipse(position.x, position.y, radius * 2, radius * 2);
            }

            g.popStyle();
        }
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active && live) {
            boolean colliding_inner = Collision.circular(agent.position, agent.radius, position, radius / 2);
            boolean colliding_outer = Collision.circular(agent.position, agent.radius, position, radius);
            if (colliding_inner) {
                agent.damageLethal(100);
                agent.addImpulse(position, FORCE*2);
            }
            if (colliding_outer) {
                agent.addImpulse(position, FORCE);
            }
            return colliding_outer;
        }
        return false;
    }
}
