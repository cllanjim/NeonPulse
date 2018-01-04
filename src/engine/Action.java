package engine;

import processing.core.PGraphics;

public interface Action {
    void ready();
    void activate();
    void interrupt();

    void update(float delta_time);
    void display(PGraphics graphics);
    void collideWithAgent(Agent agent);
}
