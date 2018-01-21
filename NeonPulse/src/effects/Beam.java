package effects;

import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

public class Beam extends Effect {
    private final PVector segment;
    private final PVector endPosition;
    private final SoundFile sound;
    private Beam next;
    private float length;

    public static final float LENGTH = 360;
    private static final float FORCE = 64;
    private static final float LIFESPAN = 0.1f;

    public Beam(float length, SoundFile beam_sound) {
        sound = beam_sound;
        segment = new PVector(0, 0);
        endPosition = new PVector(0, 0);
        this.length = length;
        next = null;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (Collision.lineCircle(position, endPosition, agent.position, agent.radius)) {
                endPosition.set(agent.position);
                agent.impulse.add(PVector.sub(agent.position, position).setMag(FORCE));
                agent.damageLethal(100);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean collideWithTile(Tile tile) {
        return false;
    }

    public void activate(PVector source, PVector target) {
        position.set(source);
        sound.play();
        lifetime = 0;
        segment.set(PVector.sub(target, source).setMag(LENGTH));
        endPosition.set(PVector.add(source, segment));
        active = true;
        live = true;
    }

    public void update(float delta_time) {
        if (active) {
            if (lifetime > 0) live = false;
            if (lifetime > LIFESPAN) {
                active = false;
            }
            lifetime += delta_time;
        }
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.stroke(0xffaa0000);
            g.strokeWeight(8);
            g.line(position.x, position.y, endPosition.x, endPosition.y);
            g.popStyle();
        }
    }

    public void setLength(float length) {
        segment.setMag(length);
        endPosition.set(PVector.add(position, segment));
    }

    public PVector getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(PVector endPosition) {
        this.endPosition.set(endPosition);
    }

    public Beam getNext() {
        return next;
    }

    public void setNext(Beam next) {
        this.next = next;
    }

    public float getLength() {
        return length;
    }
}
