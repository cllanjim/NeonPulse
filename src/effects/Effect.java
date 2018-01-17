package effects;

import engine.Agent;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

public abstract class Effect {
    PVector position;
    boolean active;
    float lifetime;
    boolean live;

    public Effect() {
        position = new PVector(0,0);
        active = false;
        live = false;
        lifetime = 0;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLive() {
        return active;
    }

    public float getLifetime() {
        return lifetime;
    }

    public PVector getPosition() {
        return position;
    }

    public abstract boolean collideWithAgent(Agent agent);
    public abstract boolean collideWithTile(Tile tile);

    public abstract void activate(PVector source, PVector target);
    public abstract void update(float delta_time);
    public abstract void display(PGraphics g);
}

