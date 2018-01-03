package util;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

class Chain {
    private final ArrayList<Link> links = new ArrayList<>(4);

    Chain(PVector position, int length, int num_links) {
        links.add(new Link(position, 0, 0));
        for (int i = 1; i < num_links; i++) {
            links.add(new Link(links.get(i - 1).end_point, length, 0));
        }
    }

    public void follow(PVector target) {
        int last_index = links.size() - 1;
        links.get(last_index).follow(target);
        for (int i = last_index - 1; i >= 0; i--) {
            links.get(i).follow(links.get(i + 1).start_point);
        }
    }

    public void show(PGraphics g, float weight) {
        for (Link link : links) {
            link.show(g, weight);
        }
    }

    static class Link {
        PVector start_point;
        PVector end_point;
        private float length;
        private float angle;

        Link(PVector start, float _length, float _angle) {
            start_point = new PVector(start.x, start.y);
            end_point = new PVector(start.x, start.y);
            length = _length;
            angle = _angle;
            setEndPoint();
        }

        private void setEndPoint() {
            float x_offset = length * PApplet.cos(angle);
            float y_offset = length * PApplet.sin(angle);
            end_point.set(start_point.x + x_offset, start_point.y + y_offset);
        }

        public void follow(PVector target) {
            PVector direction = PVector.sub(target, start_point).normalize().mult(length);
            angle = direction.heading();
            start_point.set(PVector.sub(target, direction));
            end_point.set(target);
        }

        public void show(PGraphics g, float weight) {
            g.pushStyle();
            g.strokeWeight(weight);
            g.line(start_point.x, start_point.y, end_point.x, end_point.y);
            g.popStyle();
        }
    }
}
