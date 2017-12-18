package effects;

import processing.sound.SoundFile;
import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;

public class Area extends Effect {
    private float angle;
    private float radius;

    private static final float RANGE = 64;
    private static final float LIFESPAN = 1;
    static final float COOLDOWN = 4;

    public Area(SoundFile area_sound) {
        super(area_sound);
        angle = 0;
        radius = RANGE;
    }

    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (Collision.circular(position, radius, agent.position, agent.radius)) {
                agent.impulse.set(0, 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    public void activate(PVector source, PVector target) {
        if (cooldown <= 0) {
            position.set(source);
            angle = PVector.sub(target, source).heading();
            sound.play();
            active = true;
            lifetime = 0;
            cooldown = COOLDOWN;
        }
    }

    public void update(float delta_time) {
        if (active) {
            lifetime += delta_time;
            if (lifetime > LIFESPAN) {
                active = false;
            }
        }
        cooldown -= delta_time;
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.fill(0xff0000ff, 31);
            g.ellipse(position.x, position.y, radius * 2, radius * 2);
            g.popStyle();
        }
    }
}
