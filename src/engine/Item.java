package engine;

import processing.core.PGraphics;

public interface Item {
    void ready();
    void activate();
    void interrupt();

    void update(float delta_time);
    void display(PGraphics graphics);
    boolean collideWithAgent(Agent agent);
}
