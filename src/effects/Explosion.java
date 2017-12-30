package effects;

import engine.Draw;
import processing.sound.SoundFile;
import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;

public class Explosion extends Effect {
    private float radius;

    private static final float LIFESPAN = 0.2f;
    private static final float FORCE = 256;

    public Explosion(SoundFile explosion_sound) {
        super(explosion_sound);
        radius = 128;
    }

    public void activate(PVector source, PVector target) {
        position.set(source);
        sound.play();
        active = true;
        live = true;
        lifetime = 0;
    }

    public void update(float deltatime) {
        if (active) {
            if (lifetime > 0) live = false;
            if (lifetime > LIFESPAN) active = false;
            lifetime += deltatime;
        }
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.noStroke();
            g.fill(191, 0, 31);
            Draw.polygon(g, position.x, position.y, radius + lifetime * 10, 8, 0);

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
            boolean colliding_inner = Collision.circular(agent.position, agent.radius, position, radius * 0.2f);
            boolean colliding_outer = Collision.circular(agent.position, agent.radius, position, radius * 1.1f);
            if (colliding_inner) {
                agent.damageLethal(30);
                agent.addImpulse(position, FORCE*2);
            }
            if (colliding_outer) {
                agent.damageLethal(20);
                agent.addImpulse(position, FORCE);
            }
            return colliding_outer;
        }
        return false;
    }
}
