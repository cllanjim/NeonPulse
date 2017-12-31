package effects;

import processing.core.PGraphics;
import engine.Agent;

public interface Action {
    void ready();
    void activate();
    void interrupt();

    void update(float delta_time);
    void display(PGraphics graphics);
    void collideWithAgent(Agent agent);
}
