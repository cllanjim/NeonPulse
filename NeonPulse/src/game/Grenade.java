package game;

import engine.*;
import effects.Explosion;
import effects.Payload;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.ArrayList;

import static processing.core.PApplet.*;

public class Grenade implements Item {
    public final Payload payload;
    private final Player player;
    private final SoundFile sound;
    private final PVector controlDirection;
    private final PVector targetDirection;
    private boolean aiming;

    private static final float SPEED = 768;
    private static final float MAX_FORCE = 2 * SPEED / Payload.DELAY;

    private static float relativeAngle(PVector v1, PVector v2) {
        return (atan2(v1.x*v2.y-v2.x*v1.y,v1.x*v2.x+v1.y*v2.y) + PI) % TWO_PI - PI;
    }

    Grenade(Player player, SoundFile grenade_sound, PApplet pApplet, Lighting lighting) {
        payload = new Payload(new Explosion(grenade_sound, lighting), pApplet, lighting);
        this.player = player;
        sound = grenade_sound;
        controlDirection = new PVector(0, 0);
        targetDirection = new PVector(0, 0);
        aiming = false;
    }

    @Override
    public void ready() {
        if (player.apManager.currentAP() < 1) return;
        if (!aiming && !payload.isActive()) {
            controlDirection.set(PVector.sub(player.target, player.position).normalize());
            targetDirection.set(PVector.sub(player.target, player.position).normalize());
            aiming = true;
        } else if (aiming && !payload.isActive()) {
            targetDirection.set(PVector.sub(player.target, player.position).normalize());
        }
    }

    @Override
    public void activate() {
        if (aiming && !payload.isActive()) {
            aiming = false;
            float control_angle = relativeAngle(controlDirection, targetDirection);
            float control_arc = PVector.angleBetween(controlDirection, targetDirection);
            int direction = (control_angle > 0) ? 1 : -1;
            float inward_force = MAX_FORCE * sin(control_arc);
            float forward_force = 2 * MAX_FORCE * sin(control_arc / 2);
            PVector launch_vector = controlDirection.setMag(SPEED).add(player.velocity);
            PVector forward_vector = PVector.mult(targetDirection, forward_force);
            PVector inward_vector = PVector.mult(targetDirection.rotate(PI/2), inward_force * direction);
            payload.setAcceleration(inward_vector.x + forward_vector.x, inward_vector.y + forward_vector.y);
            payload.activate(player.position, launch_vector);
            player.apManager.spendActionPoint();
            sound.play();
        }
    }

    @Override
    public void interrupt() {

    }

    public void updateMovement(float delta_time) {
        payload.updateMovement(delta_time);
    }

    public void update(float delta_time) {
        payload.update(delta_time);
    }

    public void display(PGraphics g) {
        if (aiming) {
            g.pushStyle();
            g.fill(player.fill);
            PVector aim_pos = PVector.add(player.position, PVector.mult(controlDirection, 64));
            g.ellipse(aim_pos.x, aim_pos.y, 10, 10);

            PVector launch_pos = PVector.add(player.position, PVector.mult(targetDirection, 64));
            g.ellipse(launch_pos.x, launch_pos.y, 15, 15);
            g.popStyle();
        }
        payload.display(g);
    }

    public boolean collideWithAgent(Agent agent) {
        return payload.collideWithAgent(agent);
    }

    private boolean collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        float min_distance_x = payload.radius + tile_width / 2;
        float min_distance_y = payload.radius + tile_height / 2;
        float distance_x = payload.getPosition().x - tile_position.x;
        float distance_y = payload.getPosition().y - tile_position.y;
        float x_depth = min_distance_x - PApplet.abs(distance_x);
        float y_depth = min_distance_y - PApplet.abs(distance_y);
        if (x_depth > 0 || y_depth > 0) {
            if (max(x_depth, 0) < max(y_depth, 0)) {
                // Left or right?
                if (distance_x < 0) {
                    payload.getPosition().x -= x_depth;
                } else {
                    payload.getPosition().x += x_depth;
                }
                payload.velocity.set(-payload.velocity.x, payload.velocity.y);
            } else {
                // Top or bottom?
                if (distance_y < 0) {
                    payload.getPosition().y -= y_depth;
                } else {
                    payload.getPosition().y += y_depth;
                }
                payload.velocity.set(payload.velocity.x, -payload.velocity.y);
            }
            return true;
        }
        return false;
    }

    public void collideWithLevel(Level level) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        // Wrap if outside world
        if (payload.getPosition().x < 0) payload.getPosition().x = level.levelWidth;
        if (payload.getPosition().x > level.levelWidth) payload.getPosition().x = 0;
        if (payload.getPosition().y < 0) payload.getPosition().y = level.levelHeight;
        if (payload.getPosition().y > level.levelHeight) payload.getPosition().y = 0;

        level.checkCornerCollisions(collision_positions, payload.getPosition(), payload.radius);
        if (collision_positions.size() == 0) return;
        for (PVector pos : collision_positions) {
            if (collideWithTile(pos, level.tileWidth, level.tileHeight)) {
                return;
            }
        }
    }
}
