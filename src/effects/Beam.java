package effects;

import processing.sound.SoundFile;
import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;

public class Beam extends Effect {
    private PVector end_position;

    private static final float LENGTH = 256;
    private static final float FORCE = 128;
    private static final float LIFESPAN = 0.1f;

    public Beam(SoundFile beam_sound) {
        super(beam_sound);
        end_position = new PVector(0, 0);
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (live) {
            if (Collision.lineCircle(position, end_position, agent.position, agent.radius)) {
                end_position.set(agent.position);
                agent.impulse.add(PVector.sub(agent.position, position).setMag(FORCE));
                agent.damageLethal(50);
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
            sound.play();
            lifetime = 0;
            PVector direction = PVector.sub(target, source).setMag(LENGTH);
            end_position.set(PVector.add(source, direction));
            active = true;
            live = true;
            cooldown = COOLDOWN;
        }
    }

    public void update(float delta_time) {
        if(active) {
            if (lifetime > 0) live = false;
            if (lifetime > LIFESPAN) {
                active = false;
            }
            lifetime += delta_time;
        }

        cooldown -= delta_time;
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.stroke(0xffff00ff);
            g.strokeWeight(4);
            g.line(position.x, position.y, end_position.x, end_position.y);
            g.popStyle();
        }
    }
}
