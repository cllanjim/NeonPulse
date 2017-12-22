package engine;

import processing.core.PGraphics;
import processing.core.PImage;

public class ImageTile extends Tile {
    private PImage image;
    private boolean solid;

    public ImageTile(PImage tile_image, boolean is_solid) {
        image = tile_image;
        solid = is_solid;
    }

    @Override
    public void display(PGraphics g, float tile_x, float tile_y, float tile_width, float tile_height) {
        g.pushStyle();
        g.image(image, tile_x, tile_y, tile_width, tile_height);
        g.popStyle();
    }

    @Override
    public boolean isSolid() {
        return solid;
    }
}
