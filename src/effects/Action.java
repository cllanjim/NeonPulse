package effects;

import processing.core.PGraphics;
import processing.core.PVector;
import engine.Agent;

public interface Action {
    void ready(PVector position, PVector target);
    void activate(PVector position, PVector target);
    void interrupt();

    void update(float delta_time);
    void display(PGraphics graphics);

    void collideWithAgent(Agent agent);
}
