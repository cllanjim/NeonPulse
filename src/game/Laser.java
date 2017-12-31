package game;

import effects.Action;
import effects.Beam;
import engine.Agent;
import engine.Collision;
import engine.Level;
import engine.Tilemap;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.ArrayList;

public class Laser implements Action {
    private Beam beam;
    private Player player;
    private SoundFile sound;
    private float delay;
    boolean active;
    private boolean charging;

    private static final float DELAY = 1;
    private static final float TARGET_FRAME_TIME = 1.0f / 60.0f;

    Laser(Player player, SoundFile soundFile) {
        this.player = player;
        sound = soundFile;
        delay = DELAY;
        beam = new Beam(soundFile);
    }

    @Override
    public void ready() {
        if (charging && active) {
            if (delay <= 0) {
                player.apManager.spendActionPoint();
                PVector position_offset = PVector.mult(player.velocity, TARGET_FRAME_TIME);
                beam.activate(PVector.add(player.position, position_offset), player.target);
                interrupt();
                active = false;
            }
        }
        if (player.apManager.currentAP() > 0 && active) {
            charging = true;
        }
    }

    @Override
    public void activate() {
        active = true;
        interrupt();
    }

    @Override
    public void interrupt() {
        delay = DELAY;
        charging = false;
    }

    @Override
    public void update(float delta_time) {
        if (charging) {
            delay -= delta_time;
            if (player.particleSystem != null) player.particleSystem.attract();
        }
        beam.update(delta_time);
    }

    @Override
    public void display(PGraphics g) {
        beam.display(g);
    }

    @Override
    public void collideWithAgent(Agent agent) {
        beam.collideWithAgent(agent);
    }

    private boolean collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        // TODO: get tile data from map instead of instantiating this every time.
        // Although it's only on shooting
        PVector[] points = new PVector[]{
                new PVector(tile_position.x - tile_width / 2, tile_position.y - tile_height / 2),
                new PVector(tile_position.x + tile_width / 2, tile_position.y - tile_height / 2),
                new PVector(tile_position.x + tile_width / 2, tile_position.y + tile_height / 2),
                new PVector(tile_position.x - tile_width / 2, tile_position.y + tile_height / 2),
        };

        ArrayList<PVector> collision_positions = new ArrayList<>(4);
        PVector collision_point = new PVector();
        for (int i = 0; i < points.length - 1; i++) {
            if (Collision.lineSegments(player.position, beam.end_position, points[i], points[i + 1], collision_point)) {
                collision_positions.add(collision_point);
                return true;
            }
        }

        if (collision_positions.size() == 0) {
            return false;
        } else {
            collision_positions.sort((p, n) ->
                    p.dist(player.position) > n.dist(player.position)
                            ? -1 : 1);
            beam.end_position.set(collision_positions.get(0));
            // TODO: Create new beam reflected on this spot with remaining length
            return true;
        }

    }

    public void collideWithLevel(Level level) {
        // TODO: Get all tiles the laser passes over?
    }

    public void collideWithTilemap(Tilemap tilemap) {
        // TODO: Get all tiles the laser passes over? Efficiently?
    }
}
