package game;

import effects.*;
import engine.Shapes;
import processing.core.PApplet;
import processing.sound.SoundFile;
import engine.Agent;
import processing.core.PGraphics;
import processing.core.PVector;
import util.ParticleSystem;

import java.util.ArrayList;

public class Player extends Agent {
    public PlayerInput input;
    public ApManager apManager;
    public Loadout loadout;
    PVector target;

    public Grenade grenade;
    public Laser laser;
    public Launcher gun;
    public Shield shield;
    public ParticleSystem particleSystem;

    int fill = 0xffffffff;

    public static final int HEALTH = 1;
    private static final float SPEED = 96;
    private static final float RADIUS = 24;

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

        shield = new Shield(this, player_sound);
        grenade = new Grenade(this, player_sound);
        laser = new Laser(this, player_sound);
        gun = new Launcher(this, player_sound);
        apManager = new ApManager(applet, this, RADIUS * 2);
        particleSystem = new ParticleSystem(applet, position, 1);

        target = new PVector(0, 0);
        health = HEALTH;
        radius = RADIUS;
        mass = 16;
        speed = SPEED;
        angle = 0;
    }

    public void setFill(int fill) { this.fill = fill; }

    public void addEffect(String binding, Effect effect) {
        loadout.effects.add(effect);
        input.addBinding(binding, effect);
    }

    public void update(ArrayList<Player> players, float delta_time) {
        input.handleInput(this);
        impulse.mult(speed);
        apManager.update(delta_time);
        angle = PVector.sub(target, position).heading();
        updateEffects(delta_time);
    }

    private void updateEffects(float delta_time) {
        particleSystem.update(delta_time, position.x, position.y);
        grenade.update(delta_time);
        shield.update(delta_time);
        gun.update(delta_time);
        laser.update(delta_time);
        loadout.update(delta_time);
    }

    public void collideWithEffects(Player other) {
        other.grenade.collideWithAgent(this);
        other.laser.collideWithAgent(this);
        other.shield.collideWithAgent(this);
        other.gun.collideWithAgent(this);
        other.loadout.collideWithAgent(this);
    }

    public void respawn(float x, float y) {
        position.set(x, y);
        health = HEALTH;
    }

    public void display(PGraphics g) {
        Shapes.drawPlayer(g, position, angle, radius, fill);

        // Skills
        apManager.display(g);
        grenade.display(g);
        laser.display(g);
        gun.display(g);
        shield.display(g);
        loadout.display(g);
        particleSystem.display(g);
    }

    public void setParticles(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }
}