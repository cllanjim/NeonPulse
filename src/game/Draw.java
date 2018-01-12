package game;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.*;

public class Draw {
    public static void polygon(PGraphics g, float x, float y, float radius, int num_points, float rotation) {
        g.beginShape();
        for (float a = rotation; a < TWO_PI + rotation; a += TWO_PI / num_points) {
            float sx = x + PApplet.cos(a) * radius;
            float sy = y + PApplet.sin(a) * radius;
            g.vertex(sx, sy);
        }
        g.endShape(CLOSE);
    }

    public static void shadow(PGraphics g, PVector agent_position, float radius, float height, int fill) {
        g.pushMatrix();
        g.pushStyle();
        g.translate(agent_position.x, agent_position.y);
        g.noStroke();
        g.fill(fill, 127);
        g.ellipse(0, 6, (2 * radius) - height, radius - height);
        g.popStyle();
        g.popMatrix();
    }

    public static void player(PGraphics g, float x, float y, float angle, float radius, int fill) {
        g.pushMatrix();
        g.pushStyle();
        g.translate(x, y);
        PVector dir = PVector.fromAngle(angle).mult(2);
        g.fill(0xff000000);
        polygon(g, 0, 0, radius, 4, angle + PI / 4);
        g.fill(0xffffffff);
        polygon(g, 0, 0, radius, 3, angle);
        g.fill(fill);
        polygon(g, -dir.x, -dir.y, 0.75f * radius, 3, angle);
        g.popStyle();
        g.popMatrix();
    }
}
