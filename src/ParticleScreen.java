import engine.Screen;
import game.ApManager;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import util.ParticleSystem;

public class ParticleScreen extends Screen {
    ParticleSystem pa;
    PGraphics canvas;
    ApManager p1;
    PVector pPosition;

    protected ParticleScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height);
    }

    @Override
    public void load() {
        pPosition = new PVector(applet.width/2, applet.height/2);
        pa = new ParticleSystem(applet, new PVector(applet.width / 2, applet.height / 2), 0.0f, applet.PI / 2);
        p1 = new ApManager(applet, pPosition, 15);


    }

    @Override
    public void update(float deltatime) {
        //pa.attractParticle();
        //pa.emitParticle();
        //pa.attractParticleAngle();
        //pa.emitParticleAngle();
        pa.update(applet.width / 2, applet.height / 2);
        p1.update(deltatime);
    }

    @Override
    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);
        pa.display(canvas);
        p1.display(canvas);
        canvas.endDraw();
        return canvas;
    }

    @Override
    public void unload(Screen next_screen) {

    }

    @Override
    public void handleInput() {
        if (NeonPulse.g_input.isKeyReleased('a')) {

            PVector v = NeonPulse.g_input.getMousePosition();

            //pa.explodeParticleAngle();
            //pa.explodeParticleAngle(v.x, v.y);

            //pa.implodeParticleAngle();

            if (p1.currentAP() > 0) {
                p1.useAP();
                pa.implodeParticleAngle(v.x, v.y);
            }


        }
    }

}

