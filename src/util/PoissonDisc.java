package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.*;

public class PoissonDisc {
    private PApplet applet;
    private float radius = 20;
    private float samples = 10;
    private float w = radius / sqrt(2);
    private int cols;
    private int rows;

    private PVector[] grid;
    private ArrayList<PVector> active;
    private ArrayList<PVector> ordered;

    public PoissonDisc(PApplet applet, int width, int height) {
        this.applet = applet;
        this.active = new ArrayList<>();
        this.ordered = new ArrayList<>();

        // STEP 0
        cols = floor(width / w);
        rows = floor(height / w);

        grid = new PVector[cols*rows];

        // STEP 1
        float x = width / 2;
        float y = height / 2;
        int i = floor(x / w);
        int j = floor(y / w);
        PVector pos = new PVector(x, y);
        grid[i + j * cols] = pos;
        active.add(pos);
    }

    public void add(int num_elements) {
        for (int total = 0; total < num_elements; total++) {
            int num = active.size();
            if (num > 0) {
                int randIndex = floor(applet.random(num));
                PVector pos = active.get(randIndex);
                boolean found = false;
                for (int n = 0; n < samples; n++) {
                    // Random direction
                    PVector sample = PVector.random2D();
                    // Random magnitude
                    float m = applet.random(radius, 2 * radius);
                    sample.setMag(m);

                    sample.add(pos);

                    int col = floor(sample.x / w);
                    int row = floor(sample.y / w);

                    if (col >= 0 && row >= 0 && col < cols && row < rows && grid[col + row * cols] == null) {
                        boolean ok = true;
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                int index = (col + i) + (row + j) * cols;
                                PVector neighbor = grid[index];
                                if (neighbor != null) {
                                    float d = PVector.dist(sample, neighbor);
                                    if (d < radius) {
                                        ok = false;
                                    }
                                }
                            }
                        }
                        if (ok) {
                            found = true;
                            grid[col + row * cols] = sample;
                            active.add(sample);
                            ordered.add(sample);
                            break;
                        }
                    }
                }
                if (!found) {
                    active.remove(randIndex);
                }
            }
        }
    }

    public void display(PGraphics g) {
        for (int i = 0; i < ordered.size(); i++) {
            g.pushStyle();
            g.noStroke();
            g.fill(000);
            g.rect(ordered.get(i).x, ordered.get(i).y, 20, 2);
            g.popStyle();
        }
        for (int i = 0; i < active.size(); i++) {
            g.pushStyle();
            g.noStroke();
            g.fill(000);
            g.rect(active.get(i).x, active.get(i).y, 20, 2);
            g.popStyle();
        }
    }
}

