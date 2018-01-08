package effects;

import engine.Agent;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Custom extends Effect {
    private Consumer<Agent> effectFn;
    private Predicate<Agent> collisionFn;

    Custom(Consumer<Agent> effectFn, Predicate<Agent> collisionFn, SoundFile effect_sound) {
        super(effect_sound);
        this.effectFn = effectFn;
        this.collisionFn = collisionFn;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (collisionFn.test(agent)) {
                effectFn.accept(agent);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    @Override
    public void activate(PVector source, PVector target) {}

    @Override
    public void update(float delta_time) {}

    @Override
    public void display(PGraphics g) {}
}
