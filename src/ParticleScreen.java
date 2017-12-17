import engine.Screen;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import util.ParticleSystem;

public class ParticleScreen extends Screen {
    ParticleSystem pa;
    PGraphics canvas;

    protected ParticleScreen(PApplet applet) {
        super(applet);
        canvas = applet.createGraphics(applet.width, applet.height);
    }

    @Override
    public void load() {
        pa = new ParticleSystem(applet, new PVector(applet.width / 2, applet.height / 2), 0.0f, applet.PI / 2);
    }

    @Override
    public void update(float deltatime) {
        //pa.attractParticle();
        //pa.emitParticle();
        //pa.attractParticleAngle();
        //pa.emitParticleAngle();
        pa.update(applet.width / 2, applet.height / 2);
    }

    @Override
    public PGraphics render() {
        canvas.beginDraw();
        canvas.background(0);
        pa.display(canvas);
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
            pa.implodeParticleAngle(v.x, v.y);


        }
    }

}

