package effects;

import engine.Agent;
import engine.Collision;
import engine.Tile;
import game.Draw;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.function.Consumer;

public class Projectile extends Effect {
    private float heading;
    public float radius;
    private float speed;

    private static final int LIFESPAN = 1;
    private static final float FORCE = 128;

    public Projectile(PVector pos, PVector target, float _speed) {
        super(null);
        position.set(pos.x, pos.y);
        speed = _speed;
        radius = 2;
        lifetime = LIFESPAN;
        heading = PVector.sub(target, pos).heading();
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return active && tile.isSolid();
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (Collision.circular(agent.position, agent.radius, position, radius)) {
            agent.damageLethal(35);
            agent.addImpulse(position, FORCE);
            return true;
        }
        return false;
    }

    public boolean collideWithAgent(Agent agent, Consumer<Agent> effectFn) {
        if (Collision.circular(agent.position, agent.radius, position, radius)) {
            effectFn.accept(agent);
            return true;
        }
        return false;
    }

    public boolean collideWithTile(Tile tile, Consumer<Tile> effectFn) {
        if (active && tile.isSolid()) {
            effectFn.accept(tile);
            return true;
        }
        return false;
    }

    public void activate(PVector source, PVector target) { }

    public void update(float delta_time) {
        position.add(PVector.fromAngle(heading).mult(speed * delta_time));
        lifetime -= delta_time;
    }

    public void display(PGraphics g) {
        g.pushStyle();
        g.noStroke();
        g.fill(192, 63, 192);
        Draw.polygon(g, position.x, position.y, radius, 3, heading);
        g.popStyle();
    }
}
