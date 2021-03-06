package extra.postprocessing;

import ch.bildspur.postfx.Supervisor;
import ch.bildspur.postfx.pass.Pass;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class LightingPass implements Pass {
  private final PShader shader;

  public LightingPass(PApplet applet)
  {
    shader = applet.loadShader("shaders/negateFrag.glsl");
  }

  @Override
    public void prepare(Supervisor supervisor) {
  }

  @Override
    public void apply(Supervisor supervisor) {
    PGraphics pass = supervisor.getNextPass();
    supervisor.clearPass(pass);

    pass.beginDraw();
    pass.shader(shader);
    pass.image(supervisor.getCurrentPass(), 0, 0);
    pass.endDraw();
  }
}