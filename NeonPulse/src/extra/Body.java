package extra;

import processing.core.PVector;

public abstract class Body {
    protected final PVector position = new PVector();

    public PVector getPosition() {
        return position;
    }
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public static class StaticBody extends Body {
    }

    public static class PhysicsBody extends Body {
    }

    public static class KinematicBody extends Body {
    }
}
