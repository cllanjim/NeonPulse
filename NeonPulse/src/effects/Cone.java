package effects;

import engine.Agent;
import engine.Collision;
import engine.Tile;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.sound.SoundFile;

public class Cone extends Effect {
    private final SoundFile sound;
    private float range;
    private float angle;

    private static final float RANGE = 192;
    private static final float ANGULAR_WIDTH = PConstants.PI / 4.0f;
    private static final float LIFESPAN = 0.2f;
    private static final float FORCE = 192;

    public Cone(SoundFile cone_sound) {
        sound = cone_sound;
        range = 0;
        angle = 0;
    }

    @Override
    public boolean collideWithAgent(Agent agent) {
        if (active) {
            if (Collision.arcCircle(position, range, angle, ANGULAR_WIDTH, agent.position, agent.radius)) {
                agent.impulse.add(PVector.sub(agent.position, position).setMag(FORCE));
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
            angle = PVector.sub(target, source).heading();
            lifetime = 0;
            active = true;
    }

    public void update(float delta_time) {
        if (active) {
            lifetime += delta_time;
            range = PApplet.map(lifetime, 0, LIFESPAN, 0, RANGE);
            if (lifetime > LIFESPAN) {
                active = false;
            }
        }
    }

    public void display(PGraphics g) {
        if (active) {
            g.pushMatrix();
            g.pushStyle();
            g.translate(position.x, position.y);
            g.rotate(angle - ANGULAR_WIDTH / 2);
            g.noFill();
            g.strokeWeight(2);
            g.stroke(0xffffff00, 127);
            g.arc(0, 0, range * 2, range * 2, 0 , ANGULAR_WIDTH , PConstants.OPEN);
            g.popStyle();
            g.popMatrix();
        }
    }
}
