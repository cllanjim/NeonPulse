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
        pa = new ParticleSystem(applet, new PVector(3 * applet.width/4, applet.height/2));
    }

    @Override
    public void update(float deltatime) {
        pa.attractParticle();
        pa.emitParticle();
        pa.update();
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
}
