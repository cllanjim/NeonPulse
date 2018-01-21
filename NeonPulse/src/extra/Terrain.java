package extra;

import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PConstants.P3D;
import static processing.core.PConstants.PI;

public class Terrain {
    private final PApplet applet;
    private int cols;
    private int rows;
    private int scale = 60;
    private int width = 2000;
    private int height = 1600;

    private float offset = 0;
    private float[][] terrain;
    private PGraphics graphics;


    public Terrain(PApplet applet) {
        this.applet = applet;
        graphics = applet.createGraphics(applet.width, applet.height, P3D);
        cols = width / scale;
        rows = height / scale;
        terrain = new float[cols][rows];
    }

    public void update() {
        offset -= 0.1f;

        float y_offset = offset;
        for (int y = 0; y < rows; y++) {
            float x_offset = 0;
            for (int x = 0; x < cols; x++) {
                float noise = applet.noise(x_offset, y_offset);
                terrain[x][y] = PApplet.map(noise, 0, 1, -100, 100);
                x_offset += 0.3f;
            }
            y_offset += 0.3f;
        }
    }
    
    public PGraphics render() {
        graphics.beginDraw();
        graphics.clear();
        graphics.stroke(255);
        graphics.noFill();

        graphics.translate(graphics.width/2, graphics.height);
        graphics.rotateX(PI/3);
        graphics.translate(-width /2, -height /2);

        for (int y = 0; y < rows-1; y++) {
            graphics.beginShape(graphics.TRIANGLE_STRIP);
            for (int x = 0; x < cols; x++) {
                graphics.vertex(x* scale, y* scale, terrain[x][y]);
                graphics.vertex(x* scale, (y+1)* scale, terrain[x][y+1]);
            }
            graphics.endShape();
        }

        graphics.endDraw();
        return graphics;
    }
}
