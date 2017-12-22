package engine;

import processing.core.PVector;

public abstract class Camera {
    PVector origin;
    PVector dimensions;

    PVector frameOrigin;
    PVector frameDimensions;

    abstract boolean isBoxInView();
    abstract void updatePosition();
}
