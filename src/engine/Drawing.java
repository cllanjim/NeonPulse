package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.TWO_PI;

public class Drawing {
    public static void polygon(PGraphics g, float x, float y, float radius, int num_points, float rotation) {
        g.beginShape();
        for (float a = rotation; a < TWO_PI + rotation; a += TWO_PI / num_points) {
            float sx = x + PApplet.cos(a) * radius;
            float sy = y + PApplet.sin(a) * radius;
            g.vertex(sx, sy);
        }
        g.endShape(CLOSE);
    }

    public static void shadow(PGraphics g, PVector agent_position, float agent_radius, float agent_height) {
        g.pushMatrix();
        g.pushStyle();
        g.translate(agent_position.x, agent_position.y);
        g.noStroke();
        g.fill(91, 0, 193, 127);
        g.ellipse(0, 6, (2 * agent_radius) - agent_height, agent_radius - agent_height);
        g.popStyle();
        g.popMatrix();
    }
}
