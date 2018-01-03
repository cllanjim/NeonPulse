package engine;

import game.Player;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

import static processing.core.PApplet.abs;
import static sun.dc.pr.Rasterizer.TILE_SIZE;

public class StringMap extends Level {
    private final PApplet applet;
    private final PGraphics background;
    private final PGraphics foreground;
    private final ArrayList<PVector> playerSpawns;
    private final HashMap<Character, Tile> tileMap;

    private int curr_spawn_index;
    private char[][] render_data;

    public StringMap(PApplet applet, String[] level_string, HashMap<Character, Tile> level_tile_map, int size) {
        this.applet = applet;
        playerSpawns = new ArrayList<>(4);
        background = applet.createGraphics(applet.width, applet.height);
        foreground = applet.createGraphics(applet.width, applet.height);
        tileMap = level_tile_map;

        load(level_string);
    }

    public void unload() {
        playerSpawns.clear();
    }

    // TODO: load from file
    private void load(String[] level_string) {
        unload();

        // Get level data
        tileWidth = TILE_SIZE;
        tileHeight = TILE_SIZE;
        mapWidth = level_string[0].length();
        mapHeight = level_string.length;
        levelWidth = mapWidth * tileWidth;
        levelHeight = mapHeight * tileHeight;
        top = (applet.height - levelHeight) / 2;
        left = (applet.width - levelWidth) / 2;

        // Derive render data - replaces special locations with base tiles
        render_data = new char[mapHeight][mapWidth];
        for (int i = 0, n = level_string.length; i < n; i++) {
            char[] row = level_string[i].toCharArray();
            for (int j = 0, m = row.length; j < m; j++) {
                float tile_center_x = tileWidth * (j + 0.5f);
                float tile_center_y = tileHeight * (i + 0.5f);
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
                        playerSpawns.add(new PVector(tile_center_x, tile_center_y));
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
                float tile_corner_x = j * tileWidth;
                float tile_corner_y = i * tileHeight;
                Tile tile = tileMap.get(tile_char);
                if (tile != null) {
                    tile.display(background, tile_corner_x, tile_corner_y, tileWidth, tileHeight);
                } else {
                    PApplet.println("No tile found for character: ", tile_char);
                }
            }
        }
        background.endDraw();

        // Level foreground
        foreground.beginDraw();
        foreground.noFill();
        foreground.strokeWeight(2);
        foreground.stroke(0xff7f0000);
        foreground.rect(0,0, levelWidth, levelHeight);
        foreground.endDraw();
    }

    public void showBg(PGraphics g) {
        g.image(background, 0, 0);
    }

    public void showFg(PGraphics g) {
        g.image(foreground, 0, 0);
    }

    @Override
    public PVector getSpawnPoint() {
        PVector spawn_point = playerSpawns.get(curr_spawn_index);
        curr_spawn_index = (curr_spawn_index + 1) % playerSpawns.size();
        return spawn_point;
    }

    public boolean collideWithAgent(Player agent) {
        ArrayList<PVector> collision_points = new ArrayList<>(8);

        // Wrap if outside world
        if (agent.position.x < 0) agent.position.x = levelWidth;
        if (agent.position.x > levelWidth) agent.position.x = 0;
        if (agent.position.y < 0) agent.position.y = levelHeight;
        if (agent.position.y > levelHeight) agent.position.y = 0;

        // TODO: Replace with event system
        // Kill if in pit
        if(agent.alive && checkTileFor(agent.position.x, agent.position.y, 'X')) {
            agent.alive = false;
            agent.state = new Player.KilledState(getSpawnPoint());
            agent.score -= 1;
        }

        checkEdgeCollisions(collision_points, agent.position, agent.radius);
        if (collision_points.size() == 0)
            return false;

        for (PVector pos : collision_points)
            agent.collideWithTile(pos, tileWidth, tileHeight);

        collision_points.clear();

        checkCornerCollisions(collision_points, agent.position, agent.radius);

        if (collision_points.size() == 0)
            return false;

        for (PVector pos : collision_points)
            agent.collideWithTile(pos, tileWidth, tileHeight);

        return true;
    }

    public void checkCornerCollisions(ArrayList<PVector> collision_positions, PVector position, float radius) {
        // Top Left, Top Right, Bottom Right, Bottom Left
        checkPointCollision(collision_positions, position.x - radius, position.y - radius);
        checkPointCollision(collision_positions, position.x + radius, position.y - radius);
        checkPointCollision(collision_positions, position.x + radius, position.y + radius);
        checkPointCollision(collision_positions, position.x - radius, position.y + radius);
    }

    public void checkEdgeCollisions(ArrayList<PVector> collision_positions, PVector position, float radius) {
        // Top, Right, Bottom, Left
        checkPointCollision(collision_positions, position.x, position.y - radius);
        checkPointCollision(collision_positions, position.x + radius, position.y);
        checkPointCollision(collision_positions, position.x, position.y + radius);
        checkPointCollision(collision_positions, position.x - radius, position.y);
    }

    // TODO: Following methods do the same thing pretty much. Keep one only and parametrize
    private void checkPointCollision(ArrayList<PVector> collision_positions, float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);

        // Don't collide if outside world
        if (col < 0 || col >= mapWidth || row < 0 || row >= mapHeight) return;

        // TODO: Get specific tiles/references, use custom collision functions
        if (render_data[row][col] == '#') {
            PVector collision_pos = new PVector(col, row);
            collision_pos.set(collision_pos.x * tileWidth, collision_pos.y * tileHeight);
            collision_pos.add(tileWidth / 2, tileHeight / 2);
            collision_positions.add(collision_pos);
        }
    }

    public boolean checkTileFor(float x, float y, char character) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);
        return col >= 0 && col < mapWidth && row >= 0 && row < mapHeight
                && (render_data[row][col] == character);
    }

    public Tile getTileAt(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);
        if (col < 0 || col >= mapWidth || row < 0 || row >= mapHeight) return null;
        return tileMap.get(render_data[row][col]);
    }
}
