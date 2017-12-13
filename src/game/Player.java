package game;

import effects.*;
import processing.sound.SoundFile;
import engine.Agent;
import engine.Drawing;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PConstants.PI;

public class Player extends Agent {
    public PlayerInput playerInput;
    PVector target;
    ArrayList<Effect> effects;
    private ArrayList<Action> actions;

    public Grenade grenade;
    public Launcher gun;
    public Shield shield;
    private int fill = 0xffffffff;

    private static final int HEALTH = 40;

    public Player(PlayerInput input_handler, SoundFile player_sound) {
        super();
        playerInput = input_handler;
        effects = new ArrayList<>();
        actions = new ArrayList<>();

        shield = new Shield(this, player_sound);
        grenade = new Grenade(player_sound);
        gun = new Launcher(player_sound);

        target = new PVector(0, 0);
        health = HEALTH;
        radius = 16;
        mass = 16;
        speed = 64;
        angle = 0;
    }

    public void setFill(int fill) { this.fill = fill; }

    public void addEffect(String binding, Effect effect) {
        effects.add(effect);
        playerInput.addBinding(binding, effect);
    }

    public void addAction(String binding, Action action) {
        actions.add(action);
        playerInput.addBinding(binding, action);
    }

    public void update(ArrayList<Player> players, float delta_time) {
        playerInput.handleInput(this);
        impulse.mult(speed);
        angle = PVector.sub(target, position).heading();
        updateEffects(delta_time);
    }

    private void updateEffects(float delta_time) {
        grenade.update(delta_time);
        shield.update(delta_time);
        gun.update(delta_time);

        for (Action action: actions) {
            action.update(delta_time);
        }

        for (Effect effect: effects) {
            effect.update(delta_time);
        }
    }

    public void collideWithEffects(Player other) {
        other.grenade.collideWithAgent(this);
        other.shield.collideWithAgent(this);
        other.gun.collideWithAgent(this);

        for  (Effect effect: other.effects) {
            effect.collideWithAgent(this);
        }

        for  (Action action: other.actions) {
            action.collideWithAgent(this);
        }
    }

    public void respawn(float x, float y) {
        position.set(x, y);
        health = HEALTH;
    }

    public void display(PGraphics g) {
        g.pushMatrix();
        g.pushStyle();
        g.translate(position.x, position.y);
        PVector dir = PVector.fromAngle(angle).mult(2);
        g.fill(0xff000000);
        Drawing.polygon(g, 0, 0, radius, 4, angle + PI / 4);
        g.fill(0xffffffff);
        Drawing.polygon(g, 0, 0, radius, 3, angle);
        g.fill(fill);
        Drawing.polygon(g, -dir.x, -dir.y, 0.75f * radius, 3, angle);
        g.popStyle();
        g.popMatrix();

        // Skills
        grenade.display(g);
        gun.display(g);
        shield.display(g);

        // Effects

        for (Effect effect: effects) {
            effect.display(g);
        }

        for  (Action action: actions) {
            action.display(g);
        }
    }
}