package extra;

import effects.Effect;
import engine.Agent;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CustomEffect extends Effect {
    private final SoundFile sound;
    private final Consumer<Agent> effectFn;
    private final Predicate<Agent> collisionFn;

    CustomEffect(Consumer<Agent> effectFn, Predicate<Agent> collisionFn, SoundFile effect_sound) {
        sound = effect_sound;
        this.effectFn = effectFn;
        this.collisionFn = collisionFn;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (isActive()) {
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
    public void activate(PVector source, PVector target) {
        sound.play();
    }

    @Override
    public void update(float delta_time) {}

    @Override
    public void display(PGraphics g) {}
}
