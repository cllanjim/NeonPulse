package extra;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Boxes {
    private int numberOfBoxes = 20;
    private ArrayList<Box> boxes;
    private float currentTime;
    private float start;
    private float width;
    private float height;

    public Boxes(PApplet applet) {
        boxes = new ArrayList<Box>();
        width = applet.width;
        height = applet.height;

        for (int i = 0; i != numberOfBoxes; i++) {
            float w = applet.random(50, 100);
            float h = applet.random(50, 100);
            PVector s = new PVector(w, h);
            float mass;
            PVector v;
            float number = applet.random(1);
            if (number < 0.9) {
                mass = applet.random(500, 5000);
                v = new PVector(applet.random(-2, 2), applet.random(-2, 2));
            } else {
                mass = Float.POSITIVE_INFINITY;
                // Having infinite mass boxes moving around is a really bad
                // idea, since our collision handling code cannot cope with
                // cases in which a finite mass box is squeezed between two
                // infinity mass boxes (or one and a wall):
                // TODO: Handle this case
                v = new PVector(0, 0);
            }

            Box box;
            do {
                PVector r = new PVector(applet.random(0, applet.width), applet.random(0, applet.height));
                box = new Box(s, mass, r, v, i);
            } while(box.isTouchingAnyWall(applet.width, applet.height) || box.isTouching(boxes));

            boxes.add(box);
        }

        start = applet.millis();
    }

    public void update(float delta_time) {
        currentTime = 0.0f;
        do {
            Box culpritBox1 = null;
            Box culpritBox2 = null;
            float nextCollisionTime = 1.0f;

            for (int i = 0; i != boxes.size(); i++) {
                Box box1 = boxes.get(i);
                float nextCollisionWithWall = box1.nextCollisionWithWall(width, height);
                if (nextCollisionWithWall < nextCollisionTime) {
                    culpritBox1 = box1;
                    culpritBox2 = null;
                    nextCollisionTime = nextCollisionWithWall;
                }
                for (int j = i + 1; j != boxes.size(); j++) {
                    Box box2 = boxes.get(j);
                    float nextCollisionOfBoxPair = box1.nextCollisionWith(box2);
                    if (nextCollisionOfBoxPair < nextCollisionTime) {
                        culpritBox1 = box1;
                        culpritBox2 = box2;
                        nextCollisionTime = nextCollisionOfBoxPair;
                    }
                }
            }

            for (Box box : boxes)
                box.movePartial(nextCollisionTime);

            if (culpritBox1 != null) {
                if (culpritBox2 != null)
                    culpritBox1.collideWith(culpritBox2);
                else
                    culpritBox1.collideWithWall();
            }

            currentTime = nextCollisionTime;
        } while(currentTime < 1.0);
    }

    public void display(PGraphics canvas) {
        for (Box box : boxes)
            box.display(canvas);
    }
}
