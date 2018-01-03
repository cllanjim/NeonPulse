package engine;

import processing.core.PVector;

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
            points.add(new PVector(col, row));
        }
    }
}
