package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

public class Level {
    public ArrayList<PVector> player_spawn_points;
    public float tile_size;
    private PGraphics level_background;
    private PGraphics level_foreground;
    private String[] level_data;
    private char[][] render_data;
    private int level_height;
    private int level_width;

    private HashMap<Character, Tile> tile_map;

    public Level(PApplet applet, String[] level_string, HashMap<Character, Tile> level_tile_map, float size) {
        player_spawn_points = new ArrayList<>(4);
        level_background = applet.createGraphics(applet.width, applet.height);
        level_foreground = applet.createGraphics(applet.width, applet.height);
        tile_size = size;
        tile_map = level_tile_map;

        load(level_string);
    }

    public void unload() {
        // Clear spawn points
        player_spawn_points.clear();
    }

    // TODO: load from file
    private void load(String[] level_string) {
        // Clear state
        unload();

        // Get level data
        level_height = level_string.length;
        level_width = level_string[0].length();
        level_data = level_string;

        // Derive render data - replaces special locations with base tiles
        render_data = new char[level_height][level_width];
        for (int i = 0, n = level_data.length; i < n; i++) {
            char[] row = level_data[i].toCharArray();
            for (int j = 0, m = row.length; j < m; j++) {
                float tile_center_x = tile_size * (j + 0.5f);
                float tile_center_y = tile_size * (i + 0.5f);
                switch (row[j]) {
                    // Base
                    case '#':
                    case ' ':
                    case 'X': {
                        render_data[i][j] = row[j];
                    } break;
                    // Players
                    case '1':
                    case '2':
                    case '3':
                    case '4': {
                        player_spawn_points.add(new PVector(tile_center_x, tile_center_y));
                    }
                    default: {
                        render_data[i][j] = ' ';
                    } break;
                }
            }
        }
        render();
    }

    public void update(float delta_time) {

    }

    private void render() {
        level_background.beginDraw();
        for (int i = 0, n = render_data.length; i < n; i++) {
            char[] row = render_data[i];
            for (int j = 0, m = row.length; j < m; j++) {
                char tile_char = row[j];
                float tile_corner_x = j * tile_size;
                float tile_corner_y = i * tile_size;
                Tile tile = tile_map.get(tile_char);
                if(tile != null) {
                    tile.display(level_background, tile_corner_x, tile_corner_y, tile_size, tile_size);
                } else {
                    PApplet.println("No tile found for character: ", tile_char);
                }
            }
        }
        level_background.endDraw();

        // Level foreground
//        level_foreground.beginDraw();
//        for (int i = 0, n = render_data.length; i < n; i++) {
//            char[] row = render_data[i];
//            for (int j = 0, m = row.length; j < m; j++) {
//                char tile_char = row[j];
//                float tile_corner_x = j * tile_size;
//                float tile_corner_y = i * tile_size;
//                Tile tile = tile_map.get(tile_char);
//                if(tile != null) {
//                    tile.render(level_foreground, tile_corner_x, tile_corner_y, tile_size, tile_size);
//                } else {
//                    PApplet.println("No tile found for character: ", tile_char);
//                }
//            }
//        }
//        level_foreground.endDraw();
    }

    public void showBg(PGraphics g) {
        g.image(level_background, 0, 0);
    }

    public void showFg(PGraphics g) {
//        g.image(level_foreground, 0, 0);
    }

    public boolean collideWithAgent(Agent agent) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        if(checkPitCollision(agent.position.x, agent.position.y)) {
            agent.damageLethal(100);
        }

        // Top Left, Top Right, Bottom Left, Bottom Right
        checkTileCollision(collision_positions, agent.position.x - agent.radius, agent.position.y - agent.radius);
        checkTileCollision(collision_positions, agent.position.x + agent.radius, agent.position.y - agent.radius);
        checkTileCollision(collision_positions, agent.position.x - agent.radius, agent.position.y + agent.radius);
        checkTileCollision(collision_positions, agent.position.x + agent.radius, agent.position.y + agent.radius);
        if (collision_positions.size() == 0)
            return false;

        for (PVector pos : collision_positions)
            agent.collideWithTile(pos, tile_size, tile_size);

        return true;
    }

    public void checkTileCollisions(ArrayList<PVector> collision_positions, PVector position, float radius) {
        // Top Left, Top Right, Bottom Left, Bottom Right
        checkTileCollision(collision_positions, position.x - radius, position.y - radius);
        checkTileCollision(collision_positions, position.x + radius, position.y - radius);
        checkTileCollision(collision_positions, position.x - radius, position.y + radius);
        checkTileCollision(collision_positions, position.x + radius, position.y + radius);
    }

    // TODO: Following methods do the same thing pretty much. Keep one only and parametrize
    public void checkTileCollision(ArrayList<PVector> collision_positions, float x, float y) {
        int corner_x = PApplet.floor(x / tile_size);
        int corner_y = PApplet.floor(y / tile_size);

        // Don't collide if outside world
        if (corner_x < 0 || corner_x >= level_width || corner_y < 0 || corner_y >= level_height) {
            return;
        }

        // TODO: Get specific tiles/references, use custom collision functions
        if (render_data[corner_y][corner_x] == '#') {
            PVector collision_pos = new PVector(corner_x, corner_y);
            collision_pos.mult(tile_size);
            collision_pos.add(tile_size / 2, tile_size / 2);
            collision_positions.add(collision_pos);
        }
    }

    boolean checkPitCollision(float x, float y) {
        int corner_x = PApplet.floor(x / tile_size);
        int corner_y = PApplet.floor(y / tile_size);

        // Don't collide if outside world
        if (corner_x < 0 || corner_x >= level_width || corner_y < 0 || corner_y >= level_height) {
            return true;
        }

        return (render_data[corner_y][corner_x] == 'X');
    }

    // Point-Rect Collision
    boolean checkTileFor(float x, float y, char character) {
        int corner_x = PApplet.floor(x / tile_size);
        int corner_y = PApplet.floor(y / tile_size);

        // Don't collide if outside world
        if (corner_x < 0 || corner_x >= level_width || corner_y < 0 || corner_y >= level_height) {
            return false;
        }

        return (render_data[corner_y][corner_x] == character);
    }

    public Tile getTileAt(float x, float y) {
        int col = PApplet.floor(x / tile_size);
        int row = PApplet.floor(y / tile_size);
        if (col < 0 || col >= level_width || row < 0 || row >= level_height) return null;
        return tile_map.get(render_data[row][col]);
    }
}
