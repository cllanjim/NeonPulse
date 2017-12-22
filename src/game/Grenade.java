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
    private Player player;
    private SoundFile sound;
    private float speed;
    private boolean aiming;
    private PVector control_point;
    private PVector control_vector;
    private PVector aim_vector;
    private PVector target_vector;

    Grenade(Player player, SoundFile grenade_sound) {
        payload = new Payload(new Explosion(grenade_sound), grenade_sound);
        this.player = player;
        sound = grenade_sound;
        control_point = new PVector(0,0);
        control_vector = new PVector(0,0);
        aim_vector = new PVector(0,0);
        target_vector = new PVector(0,0);
        speed = 1024;
        aiming = false;
    }

    @Override
    public void ready() {
        if (player.apManager.currentAP() < 1) return;
        if (!aiming && !payload.active) {
            control_point.set(player.target);
            target_vector.set(PVector.sub(player.target, player.position).normalize());
            aim_vector.set(PVector.sub(control_point, player.position).normalize());
            aiming = true;
            sound.play();
        } else if (aiming && !payload.active) {
            // This can be done at launch only, but with it here we can add debug info / aim feedback
            control_vector.set(PVector.sub(player.target, control_point).limit(64));
            target_vector.set(PVector.sub(player.target, player.position).normalize());
            aim_vector.set(PVector.sub(control_point, player.position).normalize());
            if (target_vector.mag() > aim_vector.mag()) {
                float control_angle = PVector.angleBetween(control_vector, aim_vector);
                control_vector.mult(PApplet.sin(control_angle) * PApplet.sin(control_angle));
            }
        }
    }

    @Override
    public void activate() {
        if (aiming && !payload.active) {
            aiming = false;
            PVector launch_vector = PVector.sub(control_point, player.position).setMag(speed).add(player.velocity);
            payload.setAcceleration(control_vector.x, control_vector.y);
            payload.activate(player.position, launch_vector);
            player.apManager.spendActionPoint();
        }
    }

    @Override
    public void interrupt() {

    }

    public void update(float delta_time) {
        payload.update(delta_time);
    }

    public void display(PGraphics g) {
        if (aiming) {
            g.pushStyle();
            g.fill(player.fill);
            aim_vector.setMag(64);
            PVector aim_pos = PVector.add(player.position, aim_vector);
            g.ellipse(aim_pos.x, aim_pos.y, 10, 10);

            target_vector.setMag(64);
            PVector launch_pos = PVector.add(player.position, target_vector);
            g.ellipse(launch_pos.x, launch_pos.y, 15, 15);
            g.popStyle();
        }
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
