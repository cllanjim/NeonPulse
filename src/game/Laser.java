package game;

import engine.*;
import effects.Beam;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.ArrayList;

public class Laser implements Item {
    private final Beam beam;
    private final Player player;
    private final SoundFile sound;
    private final ArrayList<PVector> supercoverTiles;
    private float delay;
    private boolean active;
    private boolean charging;

    private static final float DELAY = 1;
    private static final float TARGET_FRAME_TIME = 1.0f / 60.0f;

    Laser(Player player, SoundFile soundFile) {
        this.player = player;
        active = true;
        sound = soundFile;
        delay = DELAY;
        beam = new Beam(Beam.LENGTH, soundFile);
        supercoverTiles = new ArrayList<>(8);
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
        Beam next_beam = beam.getNext();
        while (next_beam != null) {
            next_beam.update(delta_time);
            next_beam = next_beam.getNext();
        }
    }

    @Override
    public void display(PGraphics g) {
        beam.display(g);
        Beam next_beam = beam.getNext();
        while (next_beam != null) {
            beam.getNext().display(g);
            next_beam = next_beam.getNext();
        }
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        return beam.collideWithAgent(agent);
    }

    private boolean collideWithTile(PVector tile_position, float tile_width, float tile_height) {
        PVector[] points = new PVector[]{
                new PVector(tile_position.x - tile_width / 2, tile_position.y - tile_height / 2),
                new PVector(tile_position.x + tile_width / 2, tile_position.y - tile_height / 2),
                new PVector(tile_position.x + tile_width / 2, tile_position.y + tile_height / 2),
                new PVector(tile_position.x - tile_width / 2, tile_position.y + tile_height / 2),
        };

        PVector collision_point = new PVector();
        float closest_distance = Float.MAX_VALUE;
        float closest_line_index = -1;
        for (int i = 0; i < points.length - 1; i++) {
            if (Collision.lineSegments(player.position, beam.getEndPosition(), points[i], points[i + 1], collision_point)) {
                float point_distance = collision_point.dist(player.position);
                if (point_distance < closest_distance) {
                    closest_distance = point_distance;
                    closest_line_index = i;
                }
            }
        }

        if (closest_distance == Float.MAX_VALUE || closest_line_index == -1) {
            return false;
        } else {
            // TODO: Create new beam reflected on this spot with remaining length
            beam.setLength(closest_distance);
            return true;
        }
    }

    public void collideWithLevel(Level level) {
        if (beam.isLive()) {
            supercoverTiles.clear();
            level.getGridPoints(supercoverTiles, player.position, beam.getEndPosition());
            int num_tiles = supercoverTiles.size();
            if (num_tiles > 0) {
                for (PVector supercoverTile : supercoverTiles) {
                    PVector tile_pos = supercoverTile
                            .mult(level.tileWidth)
                            .add(level.tileWidth / 2, level.tileHeight / 2);
                    if (level.checkCollision(tile_pos.x, tile_pos.y)
                            && collideWithTile(tile_pos, level.tileWidth, level.tileHeight)) {
                        break;
                    }
                }
            }
        }
    }
}
