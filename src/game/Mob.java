package game;

import effects.Effect;
import engine.Agent;
import engine.Drawing;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PConstants.MAX_FLOAT;

public class Mob extends Agent {
    private float range;

    public Mob(float x, float y) {
        position = new PVector(x, y);
        velocity = new PVector(0, 0);
        impulse = new PVector(0, 0);
        health = 100;
        radius = 16;
        mass = 1;
        range = 200;
        speed = 32;
        angle = 0;
    }

    public void update(ArrayList<Player> players, float delta_time) {
        // AI
        float closest_player_distance = MAX_FLOAT;
        PVector target = new PVector(0,0);

        for (Player player : players) {
            // TODO: Only stop following after twice range
            float distance = PVector.dist(player.position, position);
            if(distance < closest_player_distance) {
                closest_player_distance = distance;
                target.set(player.position);
            }
        }

        // Follow player if it is within range
        if (closest_player_distance < range) {
            setTarget(target);
        } else {
            impulse.set(0,0);
        }

        // Collisions
        for (Player player : players) {
            // Player Collision
            if(collideWithAgent(player)) {
                impulse.set(0,0);
                velocity.set(0,0);
            }

            player.grenade.collideWithAgent(this);
            player.shield.collideWithAgent(this);
            player.gun.collideWithAgent(this);

            for(Effect effect: player.effects) {
                effect.collideWithAgent(this);
            }
        }
    }

    public void respawn(float x, float y) {
        position.set(x, y);
        velocity.set(0, 0);
        impulse.set(0, 0);
        health = 100;
    }

    private void setTarget(PVector target_position) {
        PVector direction = PVector.sub(target_position, position).normalize();
        angle = direction.heading();
        impulse.set(direction).mult(speed);
    }

    public void display(PGraphics g) {
        Drawing.shadow(g, position, radius, 0);
        g.pushMatrix();
        g.translate(position.x, position.y);
        g.fill(102, 0, 31);
        Drawing.polygon(g, 0, 0, radius, 5, angle);
        g.popMatrix();
    }
}
