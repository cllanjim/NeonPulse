package effects;

import processing.sound.SoundFile;
import engine.Agent;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Effect {
    public final PVector position;
    public boolean active;
    public float lifetime;
    SoundFile sound;
    boolean live;
    float cooldown;

    static final float COOLDOWN = 2;

    Effect(SoundFile effect_sound) {
        position = new PVector(0,0);
        sound = effect_sound;
        active = false;
        live = false;
        lifetime = 0;
        cooldown = 0;
    }

    public abstract boolean collideWithAgent(Agent agent);
    public abstract boolean collideWithTile(Tile tile);

    public abstract void activate(PVector source, PVector target);
    public abstract void update(float delta_time);
    public abstract void display(PGraphics g);
}

