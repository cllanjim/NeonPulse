package game;

import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PApplet.*;

public class ApManager {
    private PApplet applet;
    private Player player;
    private float angularVelocity;
    private float actionPoints;
    private float actionTimer;
    private float radius;
    private float angle;

    static final float CIRCLE_RADIUS = 12;
    static final float ACTION_POINTS = 3;
    static final float CHARGE_DELAY = 2;

    public ApManager(PApplet applet, Player player, float radius)
    {
        this.applet = applet;
        this.player = player;
        this.radius = radius;
        actionPoints = ACTION_POINTS;
        angle = 0;
        angularVelocity = TWO_PI;
    }

    public void display(PGraphics g)
    {
        g.pushMatrix();
        g.pushStyle();
        g.translate(player.position.x, player.position.y);
        g.rotate(angle);
        g.fill(player.fill);
        if (actionPoints >=1)
        {
            g.ellipse(radius * cos(2*PI/3), radius * sin(2*PI/3), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        if (actionPoints >=2)
        {
            g.ellipse(radius * cos(4*PI/3), radius * sin(4*PI/3), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        if (actionPoints >=3)
        {
            g.ellipse(radius * cos(2*PI), radius * sin(2*PI), CIRCLE_RADIUS, CIRCLE_RADIUS);
        }
        g.popStyle();
        g.popMatrix();
    }

    public void update(float delta_time)
    {
        angle += angularVelocity * delta_time;

        if (actionPoints < ACTION_POINTS)
        {
            actionTimer += delta_time;
        }

        if (actionTimer > CHARGE_DELAY)
        {
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
        if (actionPoints > 0)
        {
            actionPoints--;
            actionTimer = 0;
        }
    }

}
