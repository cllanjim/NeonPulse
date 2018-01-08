package game;

import effects.Effect;
import effects.Explosion;
import engine.Agent;
import processing.core.PGraphics;
import processing.core.PVector;

public class Hazard {
    PVector position;
    Explosion explosion;
    float radius;

    public Hazard() {

    }

    public boolean collideWithEffect(Effect effect) {
        return false;
    }

    public boolean collideWithAgent(Agent agent) {
        return false;

    }

    public void update(float delta_time) {

    }

    public void display(PGraphics g) {

    }
}
