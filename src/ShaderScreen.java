import ch.bildspur.postfx.PostFXSupervisor;
import ch.bildspur.postfx.builder.PostFX;
import ch.bildspur.postfx.pass.BlurPass;
import ch.bildspur.postfx.pass.SobelPass;
import engine.GameScreen;
import postprocessing.LightingPass;
import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PConstants.*;

public class ShaderScreen extends GameScreen {
    private final PGraphics canvas;
    private final PostFXSupervisor supervisor;
    private final SobelPass sobelPass;
    private final LightingPass lightingPass;
    private final BlurPass blurPass;

    private float rotationX = 0;
    private float rotationY = 0;

    ShaderScreen(PApplet applet, PostFXSupervisor applet_supervisor) {
        super(applet);
        supervisor = applet_supervisor;
        canvas = applet.createGraphics(applet.width, applet.height, P3D);
        sobelPass = new SobelPass(applet);
        blurPass = new BlurPass(applet);
        lightingPass = new LightingPass(applet);
    }

    @Override
    public void load() {
    }

    @Override
    public void update(float deltatime) {
        rotationX += 0.01;
        rotationY += 0.01;
    }

    @Override
    public PGraphics render() {
        // draw a simple rotating cube around a sphere
        canvas.beginDraw();
        canvas.background(55);

        canvas.pushMatrix();

        canvas.translate(canvas.width / 2, canvas.height / 2);
        canvas.rotateX(rotationX);
        canvas.rotateY(rotationY);

        canvas.noStroke();
        canvas.fill(20, 20, 20);
        canvas.box(100);

        canvas.fill(150, 255, 255);
        canvas.sphere(60);

        canvas.popMatrix();
        canvas.endDraw();

        return canvas;
    }

    // TODO: Supervisor + lighting pass
    public void renderFX(PostFX fx) {
        applet.blendMode(SCREEN);
        supervisor.render(canvas);
        supervisor.pass(sobelPass);
        supervisor.pass(lightingPass);
        supervisor.pass(blurPass);
        supervisor.compose();
        applet.blendMode(BLEND);
    }

    @Override
    public void unload() {

    }
}
