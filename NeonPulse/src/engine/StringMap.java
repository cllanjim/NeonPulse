package engine;

import game.Player;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

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
        tileWidth = size;
        tileHeight = size;
        load(level_string);
    }

    public void unload() {
        playerSpawns.clear();
    }

    // TODO: load from file
    private void load(String[] level_string) {
        unload();

        mapWidth = level_string[0].length();
        mapHeight = level_string.length;
        levelWidth = mapWidth * tileWidth;
        levelHeight = mapHeight * tileHeight;
        offsetY = (applet.height - levelHeight) / 2;
        offsetX = (applet.width - levelWidth) / 2;

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
                    }
                    break;
                    // Players
                    case '1':
                    case '2':
                    case '3':
                    case '4': {
                        playerSpawns.add(new PVector(tile_center_x, tile_center_y));
                    }
                    default: {
                        render_data[i][j] = ' ';
                    }
                    break;
                }
            }
        }
        render();
    }

    public void update(float delta_time) {

    }

    private void render() {
        background.beginDraw();

        background.translate(offsetX, offsetY);

        // Wrapped exterior tiles
        int num_h = offsetX / tileWidth + 1;
        int num_v = offsetY / tileHeight + 1;

        for (int i = 0, n = render_data.length; i < n; i++) {
            char[] row = render_data[i];

            for (int j = 0; j < row.length; j++) {
                char tile_char = row[j];
                int corner_x = j * tileWidth;
                int corner_y = i * tileHeight;

                renderTile(tile_char, corner_x, corner_y);

                // Wrap Sides
                int opp_col_index = mapWidth - (j + 1);
                if (j < num_h) {
                    renderTile(row[opp_col_index], -tileWidth * (j + 1), corner_y);
                } else if (j > mapWidth - num_h) {
                    renderTile(row[opp_col_index], (mapWidth + opp_col_index) * tileWidth, corner_y);
                }

                // Wrap Top and Bottom
                int opp_row_index = mapHeight - (i + 1);
                if (i < num_v) {
                    row = render_data[opp_row_index];
                    renderTile(row[j], corner_x, -tileHeight * (i + 1));
                } else if (i > mapHeight - num_v) {
                    row = render_data[opp_row_index];
                    renderTile(row[j], corner_x, (mapHeight + opp_row_index) * tileHeight);
                }
            }
            // Corners
        }

        background.endDraw();

        // Level foreground
        foreground.beginDraw();
        foreground.noFill();
        foreground.strokeWeight(2);
        foreground.stroke(0xff7f0000);
        foreground.rect(0, 0, levelWidth, levelHeight);
        foreground.endDraw();
    }

    private void renderTile(char tile_char, int corner_x, int corner_y) {
        Tile tile = tileMap.get(tile_char);
        if (tile != null) {
            tile.display(background, corner_x, corner_y, tileWidth, tileHeight);
        } else {
            PApplet.println("No tile found for character: ", tile_char);
        }
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

    public boolean collideWithPlayer(Player player) {
        ArrayList<PVector> collision_points = new ArrayList<>(8);

        checkEdgeCollisions(collision_points, player.position, player.radius);
        if (collision_points.size() != 0)
            for (PVector pos : collision_points)
                player.collideWithTile(pos, tileWidth, tileHeight);

        collision_points.clear();

        checkCornerCollisions(collision_points, player.position, player.radius);

        if (collision_points.size() == 0)
            return false;

        for (PVector pos : collision_points)
            player.collideWithTile(pos, tileWidth, tileHeight);

        return true;
    }

    public boolean checkPositionFor(float x, float y, char character) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);
        return col >= 0 && col < mapWidth && row >= 0 && row < mapHeight
                && (render_data[row][col] == character);
    }

    public boolean checkCollision(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);
        return col >= 0 && col < mapWidth && row >= 0 && row < mapHeight
                && (render_data[row][col] == '#');
    }
}
