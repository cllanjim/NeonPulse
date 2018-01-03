package engine;

import processing.core.PVector;

public abstract class Camera {
    PVector origin;
    PVector dimensions;

    PVector frameOrigin;
    PVector frameDimensions;

    abstract boolean isBoxInView(float x, float y, float width, float height);
    abstract void updatePosition(float x, float y);
}
