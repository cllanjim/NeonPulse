package game;

import effects.Action;
import engine.Draw;
import processing.sound.SoundFile;
import engine.Agent;
import processing.core.PGraphics;
import processing.core.PVector;

public class Shield implements Action {
    private boolean active;
    private SoundFile sound;
    private Player player;
    private float radius;
    private float lifetime;

    private static final float LIFESPAN = 1;

    public Shield(Player shield_player, SoundFile shield_sound) {
        active = false;
        sound = shield_sound;
        lifetime = 0;
        radius = 32;
        player = shield_player;
    }

    @Override
    public void ready() {

    }

    @Override
    public void activate() {
        if (player.apManager.currentAP() > 0) {
            sound.play();
            player.shielded = true;
            player.apManager.spendActionPoint();
            active = true;
            lifetime = 0;
        }
    }

    @Override
    public void interrupt() {
        active = false;
        player.shielded = false;
    }

    public void update(float deltatime) {
        if (active) {
            lifetime += deltatime;
            if (lifetime > LIFESPAN) {
                interrupt();
            }
        }
    }

    @Override
    public void display(PGraphics g) {
        if (active) {
            g.pushStyle();
            g.fill(102, 102, 0, 127);
            float adjusted_radius = radius - 8 * (LIFESPAN - lifetime) / 200;
            Draw.polygon(g, player.position.x, player.position.y, adjusted_radius, 8, 0);
            g.popStyle();
        }
    }

    public void collideWithAgent(Agent agent) {
        if (active) {
            float collision_distance = radius + agent.radius;
            PVector distance_vec = PVector.sub(player.position, agent.position);
            float distance = distance_vec.mag();
            float collision_depth = collision_distance - distance;
            if ( collision_depth > 0 ) {
                PVector collision_vec = distance_vec.setMag(collision_depth);
                agent.position.add(PVector.mult(collision_vec,-1));
            }
        }
    }
}
