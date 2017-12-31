package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

public class Level {
    private PApplet applet;
    private ArrayList<PVector> player_spawns;
    private int curr_spawn_index;
    private PGraphics background;
    private PGraphics foreground;
    private char[][] render_data;
    public int tile_size;
    private int level_rows;
    private int level_columns;
    public int level_width;
    public int level_height;
    public float top;
    public float left;

    private HashMap<Character, Tile> tile_map;

    public Level(PApplet applet, String[] level_string, HashMap<Character, Tile> level_tile_map, int size) {
        this.applet = applet;
        player_spawns = new ArrayList<>(4);
        background = applet.createGraphics(applet.width, applet.height);
        foreground = applet.createGraphics(applet.width, applet.height);
        tile_size = size;
        tile_map = level_tile_map;

        load(level_string);
    }

    public void unload() {
        // Clear spawn points
        player_spawns.clear();
    }

    // TODO: load from file
    private void load(String[] level_string) {
        // Clear state
        unload();

        // Get level data
        level_rows = level_string.length;
        level_columns = level_string[0].length();
        level_width = level_columns * tile_size;
        level_height = level_rows * tile_size;

        top = (applet.height - level_height) / 2;
        left = (applet.width - level_width) / 2;

        // Derive render data - replaces special locations with base tiles
        render_data = new char[level_rows][level_columns];
        for (int i = 0, n = level_string.length; i < n; i++) {
            char[] row = level_string[i].toCharArray();
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
                        player_spawns.add(new PVector(tile_center_x, tile_center_y));
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
        background.beginDraw();
        for (int i = 0, n = render_data.length; i < n; i++) {
            char[] row = render_data[i];
            for (int j = 0, m = row.length; j < m; j++) {
                char tile_char = row[j];
                float tile_corner_x = j * tile_size;
                float tile_corner_y = i * tile_size;
                Tile tile = tile_map.get(tile_char);
                if(tile != null) {
                    tile.display(background, tile_corner_x, tile_corner_y, tile_size, tile_size);
                } else {
                    PApplet.println("No tile found for character: ", tile_char);
                }
            }
        }
        background.endDraw();

        // Level foreground
//        foreground.beginDraw();
//        for (int i = 0, n = render_data.length; i < n; i++) {
//            char[] row = render_data[i];
//            for (int j = 0, m = row.length; j < m; j++) {
//                char tile_char = row[j];
//                float tile_corner_x = j * tile_size;
//                float tile_corner_y = i * tile_size;
//                Tile tile = tile_map.get(tile_char);
//                if(tile != null) {
//                    tile.render(foreground, tile_corner_x, tile_corner_y, tile_size, tile_size);
//                } else {
//                    PApplet.println("No tile found for character: ", tile_char);
//                }
//            }
//        }
//        foreground.endDraw();
    }

    public void showBg(PGraphics g) {
        g.image(background, 0, 0);
    }

    public void showFg(PGraphics g) {
//        g.image(foreground, 0, 0);
    }

    public PVector getPlayerSpawn() {
        PVector spawn_point = player_spawns.get(curr_spawn_index);
        curr_spawn_index = (curr_spawn_index + 1) % player_spawns.size();
        return spawn_point;
    }

    public boolean collideWithAgent(Agent agent) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        // Wrap if outside world
        if (agent.position.x < 0) agent.position.x = level_width;
        if (agent.position.x > level_width) agent.position.x = 0;
        if (agent.position.y < 0) agent.position.y = level_height;
        if (agent.position.y > level_height) agent.position.y = 0;

        // Kill if in pit
        // TODO: Replace with event system
        if(checkPitCollision(agent.position.x, agent.position.y)) {
            agent.damageLethal(100);
            agent.score -= 1;
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
    private void checkTileCollision(ArrayList<PVector> collision_positions, float x, float y) {
        int col = PApplet.floor(x / tile_size);
        int row = PApplet.floor(y / tile_size);

        // Don't collide if outside world
        if (col < 0 || col >= level_columns || row < 0 || row >= level_rows) return;

        // TODO: Get specific tiles/references, use custom collision functions
        if (render_data[row][col] == '#') {
            PVector collision_pos = new PVector(col, row);
            collision_pos.mult(tile_size);
            collision_pos.add(tile_size / 2, tile_size / 2);
            collision_positions.add(collision_pos);
        }
    }

    private boolean checkPitCollision(float x, float y) {
        int col = PApplet.floor(x / tile_size);
        int row = PApplet.floor(y / tile_size);
        if (col < 0 || col >= level_columns || row < 0 || row >= level_rows) return true;
        return (render_data[row][col] == 'X');
    }

    // Point-Rect Collision
    public boolean checkTileFor(float x, float y, char character) {
        int col = PApplet.floor(x / tile_size);
        int row = PApplet.floor(y / tile_size);
        if (col < 0 || col >= level_columns || row < 0 || row >= level_rows) return false;
        return (render_data[row][col] == character);
    }

    public Tile getTileAt(float x, float y) {
        int col = PApplet.floor(x / tile_size);
        int row = PApplet.floor(y / tile_size);
        if (col < 0 || col >= level_columns || row < 0 || row >= level_rows) return null;
        return tile_map.get(render_data[row][col]);
    }
}
