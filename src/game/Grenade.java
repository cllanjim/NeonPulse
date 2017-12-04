package game;

import effects.Action;
import effects.Explosion;
import effects.Payload;
import engine.Tilemap;
import processing.sound.SoundFile;
import engine.Agent;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import engine.Level;

import java.util.ArrayList;

public class Grenade implements Action {
    private Payload payload;
    SoundFile sound;
    private float speed;
    private boolean aiming;
    float cooldown;
    private PVector control_vector;
    private PVector control_point;

    protected static final float COOLDOWN = 1;

    public Grenade(SoundFile grenade_sound) {
        payload = new Payload(new Explosion(grenade_sound), grenade_sound);
        sound = grenade_sound;
        control_point = new PVector(0,0);
        control_vector = new PVector(0,0);
        speed = 12;
        aiming = false;
    }

    @Override
    public void ready(PVector player_position, PVector target_position) {
        if (!aiming && !payload.active && cooldown <= 0) {
            control_point.set(target_position);
            aiming = true;
            sound.play();
        } else if (aiming && !payload.active) {
            // This can be done at launch only, but with it here we can add debug info / aim feedback
            control_vector = PVector.sub(target_position, control_point).mult(0.05f).limit(5);
            PVector final_vector = PVector.sub(target_position, player_position);
            PVector target_vector = PVector.sub(control_point, player_position);
            if (final_vector.mag() > target_vector.mag()) {
                float control_angle = PVector.angleBetween(control_vector, target_vector);
                control_vector.mult(PApplet.sin(control_angle) * PApplet.sin(control_angle));
            }
            cooldown = COOLDOWN;
        }
    }

    @Override
    public void activate(PVector player_position, PVector target_position) {
        if (aiming && !payload.active) {
            aiming = false;
            PVector target_vector = PVector.sub(control_point, player_position).setMag(speed);

            float control_force = control_vector.mag() / 32;
            payload.setAcceleration(
                    control_vector.x * control_force,
                    control_vector.y * control_force
            );
            payload.activate(player_position, target_vector);
        }
    }

    @Override
    public void interrupt() {

    }

    public void update(float delta_time) {
        payload.update(delta_time);
        cooldown -= delta_time;
    }

    public void display(PGraphics g) {
        payload.display(g);
    }

    public void collideWithAgent(Agent agent) {
        payload.collideWithAgent(agent);
    }

    private boolean collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        float min_distance_x = payload.radius + tile_width / 2;
        float min_distance_y = payload.radius + tile_height / 2;
        float dist_x = payload.position.x - tile_position.x;
        float dist_y = payload.position.y - tile_position.y;
        float x_depth = min_distance_x - PApplet.abs(dist_x);
        float y_depth = min_distance_y - PApplet.abs(dist_y);

        if (x_depth > 0 || y_depth > 0) {
            payload.activateEffect(tile_position);
            return true;
        }

        return false;
    }

    public void collideWithLevel(Level level) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);
        level.checkTileCollisions(collision_positions, payload.position, payload.radius);
        if (collision_positions.size() == 0) return;
        for (PVector pos : collision_positions) {
            if (collideWithTile(pos, level.tile_size, level.tile_size)) {
                return;
            }
        }
    }

    public void collideWithTilemap(Tilemap tilemap) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);
        tilemap.checkTileCollisions(collision_positions, payload.position, payload.radius);
        if (collision_positions.size() == 0) return;
        for (PVector pos : collision_positions) {
            if (collideWithTile(pos, tilemap.tileWidth, tilemap.tileHeight)) {
                return;
            }
        }
    }
}
