package game;

import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PApplet.*;

public class APManager {
    private final Player player;
    private float angularVelocity;
    private float actionTimer;
    private float radius;
    private float angle;
    private int actionPoints;

    private static final float CIRCLE_RADIUS = 12;
    private static final float CHARGE_DELAY = 1;
    private static final int MAX_ACTION_POINTS = 3;

    APManager(Player player, float radius) {
        this.player = player;
        this.radius = radius;
        actionPoints = MAX_ACTION_POINTS;
        angle = 0;
        angularVelocity = TWO_PI;
    }

    public void display(PGraphics g) {
        g.pushMatrix();
        g.pushStyle();
        g.translate(player.position.x, player.position.y);
        g.rotate(angle);
        g.fill(player.fill);
        if (actionPoints >= 1) {
            g.ellipse(radius * cos(2*PI/3), radius * sin(2*PI/3), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        if (actionPoints >= 2) {
            g.ellipse(radius * cos(4*PI/3), radius * sin(4*PI/3), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        if (actionPoints >= 3) {
            g.ellipse(radius * cos(2*PI), radius * sin(2*PI), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        g.popStyle();
        g.popMatrix();
    }

    public void update(float delta_time) {
        angle += angularVelocity * delta_time;

        if (actionPoints < MAX_ACTION_POINTS) {
            actionTimer += delta_time;
        }

        if (actionTimer > CHARGE_DELAY) {
            actionPoints++;
            actionTimer = 0;
        }
    }

    public float currentAP()
    {
        return actionPoints;
    }

    public void spendActionPoint()
    {
        if (actionPoints > 0) {
            actionPoints--;
            actionTimer = 0;
        }
    }

}
