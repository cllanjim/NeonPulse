package game;

import effects.Action;
import processing.sound.SoundFile;
import engine.Agent;
import engine.Drawing;
import processing.core.PGraphics;
import processing.core.PVector;

public class Shield implements Action {
    private boolean active;
    private SoundFile sound;
    private Player player;
    private float radius;
    private float lifetime;
    private float cooldown;

    private static final float LIFESPAN = 1;
    private static final float COOLDOWN = 4;

    public Shield(Player shield_player, SoundFile shield_sound) {
        active = false;
        sound = shield_sound;
        lifetime = 0;
        radius = 24;
        cooldown = 0;
        player = shield_player;
    }

    @Override
    public void ready(PVector position, PVector target) {

    }

    @Override
    public void activate(PVector position, PVector target) {
        if (cooldown <= 0) {
            sound.play();
            player.shielded = true;
            active = true;
            lifetime = 0;
            cooldown = COOLDOWN;
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
        cooldown -= deltatime;
    }

    @Override
    public void display(PGraphics g) {
        if (active) {
            g.fill(102, 102, 0, 127);
            float adjusted_radius = radius - 8 * (LIFESPAN - lifetime) / 200;
            Drawing.polygon(g, player.position.x, player.position.y, adjusted_radius, 8, 0);
        }
    }

    public void collideWithAgent(Agent agent) {
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
