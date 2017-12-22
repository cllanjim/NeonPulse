package engine;

import game.Player;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.abs;

public abstract class Agent {
    public PVector position;
    public PVector velocity;
    public PVector impulse;
    public float radius;
    public int health;
    public float mass;
    public boolean shielded;
    public int score = 0;
    protected float speed;
    protected float angle;

    LooseGrid.Cell owner_cell;
    int cell_array_index;

    protected Agent() {
        position = new PVector(0,0);
        velocity = new PVector(0,0);
        impulse = new PVector(0,0);
    }

    private static final float FRICTION = 0.15f;

    abstract public void update(ArrayList<Player> players, float delta_time);
    abstract public void display(PGraphics graphics);

    public void addImpulse(PVector source, float force) {
        impulse.add(PVector.sub(position, source).normalize().mult(force));
    }

    public void moveTowards(PVector target, float speed) {
        position.add(target.setMag(speed));
    }

    public void setPosition(PVector position) {}

    public void updateMovement(float delta_time) {
        // TODO: Velocity verlet movement

        // Friction
        PVector friction = PVector.mult(velocity, FRICTION);

        // Movement
        velocity.add(impulse).sub(friction);
        position.add(PVector.mult(velocity, delta_time));
    }

    public boolean collideWithAgent(Agent other) {
        float collision_distance = radius + other.radius;
        PVector distance_vec = PVector.sub(position, other.position);
        float distance = distance_vec.mag();
        float collision_depth = collision_distance - distance;
        if ( collision_depth > 0 ) {
            PVector collision_vec = distance_vec.setMag(collision_depth);
            position.add(PVector.mult(collision_vec, 1));
            other.position.add(PVector.mult(collision_vec,-1));
            return true;
        }
        return false;
    }

    void collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        float min_distance_x = radius + tile_width / 2;
        float min_distance_y = radius + tile_height / 2;
        float distance_x = position.x - tile_position.x;
        float distance_y = position.y - tile_position.y;
        float x_depth = min_distance_x - abs(distance_x);
        float y_depth = min_distance_y - abs(distance_y);

        if (x_depth > 0 || y_depth > 0) {
            if (PApplet.max(x_depth, 0) <= PApplet.max(y_depth, 0)) {
                if (distance_x < 0) {
                    position.x -= x_depth;
                } else {
                    position.x += x_depth;
                }
            } else {
                if (distance_y < 0) {
                    position.y -= y_depth;
                } else {
                    position.y += y_depth;
                }
            }
        }
    }

    public boolean damageLethal(int damage) {
        if (shielded) return false;
        health -= damage;
        return (damage < 0);
    }
}
