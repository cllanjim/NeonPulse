package game;

import effects.*;
import engine.Input;
import engine.Level;
import engine.Light;
import processing.core.PApplet;
import processing.sound.SoundFile;
import engine.Agent;
import processing.core.PGraphics;
import processing.core.PVector;
import util.ParticleSystem;

import java.util.ArrayList;

import static processing.core.PApplet.println;
import static processing.core.PConstants.TWO_PI;

public class Player extends Agent {
    public PlayerInput input;
    public PlayerState state;
    public APManager apManager;
    public Loadout loadout;
    public PVector target;

    public Grenade grenade;
    public Laser laser;
    public Launcher gun;

    public ParticleSystem particleSystem;
    public Light light;

    public boolean alive;

    int fill = 0xffffffff;

    public static final int HEALTH = 1;
    private static final float SPEED = 96;
    private static final float RADIUS = 24;

    interface PlayerState {
        void handleInput(Player player, Input input);
        void update(Player player, float delta_time);
    }

    static class NormalState implements PlayerState {
        @Override
        public void handleInput(Player player, Input input) {
            player.input.handleInput(player);
        }

        @Override
        public void update(Player player, float delta_time) {
        }
    }

    public static class KilledState implements PlayerState {
        PVector spawn_point;
        float timer = 1;

        public KilledState(PVector spawn_point) {
            this.spawn_point = new PVector(spawn_point.x, spawn_point.y);
        }

        @Override
        public void handleInput(Player player, Input input) {
        }

        @Override
        public void update(Player player, float delta_time) {
            timer -= delta_time;
            player.impulse.set(0, 0);
            player.velocity.mult(0.5f);
            player.angle += 2 * TWO_PI * delta_time;
            if (timer < 0) {
                player.state = new Player.NormalState();
                player.alive = true;
                player.respawn(spawn_point);
            }
        }
    }

    public static final int[] PLAYER_COLORS = {
            0xffC400FF,
            0xff0C71E8,
            0xff00FF4A,
            0xffE8D90C,
            0xffFF740D
    };

    public Player(PApplet applet, PlayerInput playerInput, SoundFile player_sound) {
        super();
        input = playerInput;
        loadout = new Loadout();
        state = new NormalState();

        grenade = new Grenade(this, player_sound);
        laser = new Laser(this, player_sound);
        gun = new Launcher(this, player_sound);
        apManager = new APManager(this, RADIUS * 2);

        particleSystem = new ParticleSystem(applet, position, 1);
        light = new Light(position.x, position.y, 1024);

        target = new PVector(0, 0);
        health = HEALTH;
        radius = RADIUS;
        mass = 16;
        speed = SPEED;
        angle = 0;
        alive = true;
    }

    public void setFill(int fill) {
        this.fill = fill;
    }

    public void addEffect(String binding, Effect effect) {
        loadout.effects.add(effect);
        input.addBinding(binding, effect);
    }

    public void update(ArrayList<Player> players, float delta_time) {
        state.handleInput(this, null);
        state.update(this, delta_time);
        impulse.mult(speed);
        apManager.update(delta_time);
        updateEffects(delta_time);
    }

    private void updateEffects(float delta_time) {
        particleSystem.update(delta_time, position.x, position.y);
        grenade.update(delta_time);
        gun.update(delta_time);
        laser.update(delta_time);
        loadout.update(delta_time);
    }

    public void collideWithEffects(Player other) {
        other.grenade.collideWithAgent(this);
        other.laser.collideWithAgent(this);
        other.gun.collideWithAgent(this);
        other.loadout.collideWithAgent(this);
    }

    public void respawn(PVector spawn_point) {
        position.set(spawn_point);
        velocity.set(0, 0);
        health = HEALTH;
    }

    public void display(PGraphics g) {
        Draw.player(g, position, angle, radius, fill);

        // Skills
        apManager.display(g);
        grenade.display(g);
        laser.display(g);
        gun.display(g);
        loadout.display(g);
        particleSystem.display(g);
    }

    public void setParticles(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }
}