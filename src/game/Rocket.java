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
import util.ParticleSystem;

import java.util.ArrayList;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

public class Rocket implements Item {
    private final Payload payload;
    private final Player player;
    private final SoundFile sound;
    private final PVector target_vector;
    private final ParticleSystem particleSystem;
    private float speed;
    private boolean aiming;

    Rocket(Player player, ParticleSystem particle_system, SoundFile grenade_sound) {
        payload = new Payload(new Explosion(grenade_sound), grenade_sound);
        this.player = player;
        particleSystem = particle_system;
        sound = grenade_sound;
        target_vector = new PVector(0,0);
        speed = 512;
        aiming = false;
    }

    @Override
    public void ready() {
        if (player.apManager.currentAP() < 1) return;
        aiming = true;
        target_vector.set(PVector.sub(player.target, player.position).normalize());
        PVector launch_vector = PVector.mult(target_vector, speed).add(player.velocity);
        payload.activate(player.position, launch_vector);
        player.apManager.spendActionPoint();
    }

    @Override
    public void activate() {
        if (aiming && payload.active) {
            aiming = false;
            payload.activateEffect(player.position);
        }
    }

    @Override
    public void interrupt() {

    }

    public void update(float delta_time) {
        payload.addImpulse(PVector.sub(player.target, player.position).setMag(32));
        payload.update(delta_time);
        particleSystem.setAngle(payload.velocity.heading() + PI, PI / 6);
        particleSystem.emit();
    }

    public void display(PGraphics g) {
        if (aiming) {
            g.pushStyle();
            g.fill(player.fill);
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

    // TODO: Figure out a way to do this once - probably join Level and Tilemap
    public void collideWithLevel(StringMap level) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);
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
