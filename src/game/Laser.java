package game;

import engine.Item;
import effects.Beam;
import engine.Agent;
import engine.Collision;
import engine.StringMap;
import engine.Tilemap;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;
import ptmx.Ptmx;

import java.util.ArrayList;

import static processing.core.PApplet.floor;

public class Laser implements Item {
    private final Beam beam;
    private final Player player;
    private final SoundFile sound;
    private float delay;
    private boolean active;
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

        PVector collision_point = new PVector();
        float closest_distance = Float.MAX_VALUE;
        for (int i = 0; i < points.length - 1; i++) {
            if (Collision.lineSegments(player.position, beam.endPosition, points[i], points[i + 1], collision_point)) {
                float point_distance = collision_point.dist(player.position);
                if (point_distance < closest_distance) {
                    closest_distance = point_distance;
                }
            }
        }

        if (closest_distance == Float.MAX_VALUE) {
            return false;
        } else {
            // TODO: Create new beam reflected on this spot with remaining length
            beam.setLength(closest_distance);
            return true;
        }

    }

    public void collideWithLevel(StringMap level) {
        if (beam.active) {
            ArrayList<PVector> tiles = new ArrayList<>(8);
            level.getGridPoints(tiles, player.position, beam.endPosition);
            int num_tiles = tiles.size();
            if (num_tiles > 0) {
                for (int i = 0; i < num_tiles; i++) {
                    PVector tile_pos = tiles.get(i)
                            .mult(level.tileWidth)
                            .add(level.tileWidth / 2, level.tileHeight / 2);
                    if (level.checkTileFor(tile_pos.x, tile_pos.y, '#')
                            && collideWithTile(tile_pos, level.tileWidth, level.tileHeight)) {
                        break;
                    }
                }
            }
        }
    }

    public void collideWithTilemap(Tilemap tilemap) {
        if (beam.active) {
            ArrayList<PVector> tiles = new ArrayList<>(8);
            tilemap.getGridPoints(tiles, player.position, beam.endPosition);
            int num_tiles = tiles.size();
            if (num_tiles > 0) {
                for (PVector tile_pos : tiles) {
                    ArrayList<Ptmx.CollisionShape> shapes = tilemap.map.getShapes(0, floor(tile_pos.x), floor(tile_pos.y));
                    if (shapes != null) {
                        tile_pos.mult(tilemap.tileWidth)
                                .add(tilemap.tileWidth / 2, tilemap.tileHeight / 2);
                        if (collideWithTile(tile_pos, tilemap.tileWidth, tilemap.tileHeight)) return;
                    }
                }
            }
        }
    }
}
