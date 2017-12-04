package engine;

import processing.core.PGraphics;

public abstract class Tile {
    public abstract void display(PGraphics g, float tile_x, float tile_y, float tile_width, float tile_height);
    public abstract boolean isSolid();
}
