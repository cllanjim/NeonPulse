package game;

import engine.Item;
import effects.Explosion;
import effects.Payload;
import engine.Agent;
import engine.StringMap;
import engine.Tilemap;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.ArrayList;

import static processing.core.PApplet.max;

public class Grenade implements Item {
    private final Payload payload;
    private final Player player;
    private final SoundFile sound;
    private final PVector controlPoint;
    private final PVector controlVector;
    private final PVector aimVector;
    private final PVector targetVector;
    private float speed;
    private boolean aiming;

    Grenade(Player player, SoundFile grenade_sound) {
        payload = new Payload(new Explosion(grenade_sound), grenade_sound);
        this.player = player;
        sound = grenade_sound;
        controlPoint = new PVector(0,0);
        controlVector = new PVector(0,0);
        aimVector = new PVector(0,0);
        targetVector = new PVector(0,0);
        speed = 1024;
        aiming = false;
    }

    @Override
    public void ready() {
        if (player.apManager.currentAP() < 1) return;
        if (!aiming && !payload.active) {
            controlPoint.set(player.target);
            targetVector.set(PVector.sub(player.target, player.position).normalize());
            aimVector.set(PVector.sub(controlPoint, player.position).normalize());
            aiming = true;
        } else if (aiming && !payload.active) {
            // This can be done at launch only, but with it here we can add debug info / aim feedback
            controlVector.set(PVector.sub(player.target, controlPoint).setMag(32));
            targetVector.set(PVector.sub(player.target, player.position).normalize());
            aimVector.set(PVector.sub(controlPoint, player.position).normalize());
            float control_angle = PVector.angleBetween(controlVector, aimVector);
            controlVector.mult(PApplet.sin(control_angle));
        }
    }

    @Override
    public void activate() {
        if (aiming && !payload.active) {
            aiming = false;
            PVector launch_vector = PVector.sub(controlPoint, player.position).setMag(speed).add(player.velocity);
            payload.setAcceleration(controlVector.x, controlVector.y);
            payload.activate(player.position, launch_vector);
            player.apManager.spendActionPoint();
            sound.play();
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
            aimVector.setMag(64);
            PVector aim_pos = PVector.add(player.position, aimVector);
            g.ellipse(aim_pos.x, aim_pos.y, 10, 10);

            targetVector.setMag(64);
            PVector launch_pos = PVector.add(player.position, targetVector);
            g.ellipse(launch_pos.x, launch_pos.y, 15, 15);
            g.popStyle();

            g.line(player.position.x, player.position.y, player.position.x + controlVector.x, player.position.y + controlVector.y);
        }
        payload.display(g);
    }

    public void collideWithAgent(Agent agent) {
        payload.collideWithAgent(agent);
    }

    private boolean collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        float min_distance_x = payload.radius + tile_width / 2;
        float min_distance_y = payload.radius + tile_height / 2;
        float distance_x = payload.position.x - tile_position.x;
        float distance_y = payload.position.y - tile_position.y;
        float x_depth = min_distance_x - PApplet.abs(distance_x);
        float y_depth = min_distance_y - PApplet.abs(distance_y);
        if (x_depth > 0 || y_depth > 0) {
            if (max(x_depth, 0) < max(y_depth, 0)) {
                // Left or right?
                if (distance_x < 0) {
                    payload.position.x -= x_depth;
                } else {
                    payload.position.x += x_depth;
                }
                payload.velocity.set(-payload.velocity.x, payload.velocity.y);
            } else {
                // Top or bottom?
                if (distance_y < 0) {
                    payload.position.y -= y_depth;
                } else {
                    payload.position.y += y_depth;
                }
                payload.velocity.set(payload.velocity.x, -payload.velocity.y);
            }
            return true;
        }
        return false;
    }

    // TODO: Figure out a way to do this once - probably join Level and Tilemap
    public void collideWithLevel(StringMap level) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        // Wrap if outside world
        if (payload.position.x < 0) payload.position.x = level.levelWidth;
        if (payload.position.x > level.levelWidth) payload.position.x = 0;
        if (payload.position.y < 0) payload.position.y = level.levelHeight;
        if (payload.position.y > level.levelHeight) payload.position.y = 0;

        level.checkCornerCollisions(collision_positions, payload.position, payload.radius);
        if (collision_positions.size() == 0) return;
        for (PVector pos : collision_positions) {
            if (collideWithTile(pos, level.tileWidth, level.tileHeight)) {
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
