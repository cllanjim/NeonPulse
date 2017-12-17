package game;



import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class ApManager {
    PApplet applet;

    float angularVelocity;
    float ap;
    float apTimer;
    float centerX, centerY;
    float circSize;
    float r;

    float pSize;
    float angle;

    public ApManager(PApplet applet, PVector pos, float radius)
    {
        ap=3;
        centerX=pos.x;
        centerY=pos.y;
        angle=0;
        circSize=15;
        r=2*radius;
        angularVelocity = applet.TWO_PI;
        this.applet=applet;
    }//constructor end


    public void display(PGraphics g)
    {
        g.pushMatrix();
        g.translate(centerX, centerY);
        g.rotate(angle);

        if (ap>=1)
        {
            g.fill(200, 0, 0);
            g.ellipse(r*applet.cos(2*applet.PI/3), r*applet.sin(2*applet.PI/3), circSize, circSize);
        }
        if (ap>=2)
        {
            g.fill(0, 200, 0);
            g.ellipse(r*applet.cos(4*applet.PI/3), r*applet.sin(4*applet.PI/3), circSize, circSize);
        }
        if (ap>=3)
        {
            g.fill(0, 0, 200);
            g.ellipse(r*applet.cos(2*applet.PI), r*applet.sin(2*applet.PI), circSize, circSize);
        }

        g.popMatrix();
    }//display end

    public void update(float delta_time)
    {
        angle = angle + angularVelocity * delta_time;

        // create ap
        if (ap<3)
            apTimer += delta_time;

        if (apTimer > 1)
        {
            ap++;
            apTimer=0;
        }
    }

    public float currentAP()
    {
        return ap;
    }

    public void useAP()
    {
        if (ap>0)
        {
            ap--;
            apTimer=0;
        }
    }

}
