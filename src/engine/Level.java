package engine;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.floor;

public abstract class Level {
    public int tileWidth;
    public int tileHeight;
    public int mapWidth;
    public int mapHeight;
    public int levelWidth;
    public int levelHeight;
    public int top;
    public int left;

    public abstract PVector getSpawnPoint();
    public abstract boolean checkCollision(float x, float y);

    public PVector getTilePosition(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);
        PVector tile_pos = new PVector(col, row);
        tile_pos.set(tile_pos.x * tileWidth, tile_pos.y * tileHeight);
        tile_pos.add(tileWidth / 2, tileHeight / 2);
        return tile_pos;
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

        if (checkCollision(x, y)) {
            PVector collision_pos = new PVector(col, row);
            collision_pos.set(collision_pos.x * tileWidth, collision_pos.y * tileHeight);
            collision_pos.add(tileWidth / 2, tileHeight / 2);
            collision_positions.add(collision_pos);
        }
    }

    public void getGridPoints(List<PVector> points, PVector start_position, PVector end_position) {
        float start_col = start_position.x / tileWidth;
        float start_row = start_position.y / tileHeight;
        float end_col = end_position.x / tileWidth;
        float end_row = end_position.y / tileHeight;
        float dx = end_col - start_col;
        float dy = end_row - start_row;
        float nx = Math.abs(dx);
        float ny = Math.abs(dy);
        int sx = dx > 0 ? 1 : -1;
        int sy = dy > 0 ? 1 : -1;

        int col = floor(start_col);
        int row = floor(start_row);

        points.add(new PVector(col, row));
        for (int ix = 0, iy = 0; ix < nx || iy < ny; ) {
            if ((0.5 + ix) / nx == (0.5 + iy) / ny) {
                col += sx;
                row += sy;
                ix++;
                iy++;
            } else if ((0.5 + ix) / nx < (0.5 + iy) / ny) {
                col += sx;
                ix++;
            } else {
                row += sy;
                iy++;
            }
            if (col >= 0 && row >= 0) points.add(new PVector(col, row));
        }
    }

    public void wrapAgent(Agent agent) {
        // Wrap if outside world
        if (agent.position.x < 0) agent.position.x = levelWidth;
        if (agent.position.x > levelWidth) agent.position.x = 0;
        if (agent.position.y < 0) agent.position.y = levelHeight;
        if (agent.position.y > levelHeight) agent.position.y = 0;
    }
}
