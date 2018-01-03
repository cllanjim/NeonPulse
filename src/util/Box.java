package util;

import java.util.ArrayList;

import processing.core.*;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CORNER;

public class Box {
    int id;
    float mass;
    PVector dimensions;
    PVector halfDims;
    PVector position;
    PVector velocity;
    boolean verticalWallBounce;

    private static float currentTime = 0;

    public Box(PVector dimensions, float mass, PVector position, PVector velocity, int id) {
        this.dimensions = dimensions.copy();
        this.halfDims = PVector.div(dimensions, 2);
        this.mass = mass;
        this.position = position.copy();
        this.velocity = velocity.copy();
        this.id = id;
        this.verticalWallBounce = false;
    }

    public float nextCollisionWith(Box other) {
        PVector rv = PVector.sub(this.velocity, other.velocity);
        PVector rs = PVector.add(this.dimensions, other.dimensions);
        PVector hrs = PVector.div(rs, 2);

        PVector r1 = PVector.sub(other.position, hrs);
        PVector r2 = PVector.add(other.position, hrs);

        // The inverse of the velocity components (may result in ∞):
        float fracX = 1.0f / rv.x;
        float fracY = 1.0f / rv.y;

        // The time necessary for this box (reduced to a point) to cross each edge: left, right, top, bottom
        float timeX1 = (r1.x - this.position.x) * fracX;
        float timeX2 = (r2.x - this.position.x) * fracX;
        float timeY1 = (r1.y - this.position.y) * fracY;
        float timeY2 = (r2.y - this.position.y) * fracY;

        // The time of the first vertical edge crossing:
        float timeXEntry = min(timeX1, timeX2);
        // The time of the first horizontal edge crossing:
        float timeYEntry = min(timeY1, timeY2);
        // The time when the first horizontal and the first vertical edge crossings are done:
        float timeEntry = max(timeXEntry, timeYEntry);

        // It the time for entry is negative, the collision occurred in the past:
        if (timeEntry <= 0.0)
            return Float.POSITIVE_INFINITY;

        // The time of the second vertical edge crossing:
        float timeXExit = max(timeX1, timeX2);
        // The time of the second horizontal edge crossing:
        float timeYExit = max(timeY1, timeY2);
        // The time when the second horizontal *or* the second vertical edge
        // crossings are done:
        float timeExit = min(timeXExit, timeYExit);

        // In this case, there will be no collision:
        if (timeEntry > timeExit)
            return Float.POSITIVE_INFINITY;

        // Return the current time plus the time to the collision:
        return currentTime + timeEntry;
    }

    public void collideWith(Box other) {
        PVector rv = PVector.sub(this.velocity, other.velocity);
        PVector rs = PVector.add(this.dimensions, other.dimensions);
        PVector hrs = PVector.div(rs, 2);
        PVector r1 = PVector.sub(other.position, hrs);
        PVector r2 = PVector.add(other.position, hrs);

        float fracX = 1.0f / rv.x;
        float fracY = 1.0f / rv.y;

        float timeX1 = (r1.x - this.position.x) * fracX;
        float timeX2 = (r2.x - this.position.x) * fracX;
        float timeY1 = (r1.y - this.position.y) * fracY;
        float timeY2 = (r2.y - this.position.y) * fracY;

        float timeXEntry = min(timeX1, timeX2);
        float timeYEntry = min(timeY1, timeY2);

        if (timeYEntry > timeXEntry)
            collideVertically(other);
        else
            collideHorizontally(other);
    }

    private static float min(float v1, float v2) {
        if (v1 != v1 && v2 != v2) return 0.0f / 0.0f;
        if (v1 != v1) return Float.NEGATIVE_INFINITY;
        if (v2 != v2) return Float.NEGATIVE_INFINITY;
        return v1 < v2 ? v1 : v2;
    }

    private static float max(float v1, float v2) {
        if (v1 != v1 && v2 != v2) return 0.0f / 0.0f;
        if (v1 != v1) return Float.POSITIVE_INFINITY;
        if (v2 != v2) return Float.POSITIVE_INFINITY;
        return v1 < v2 ? v2 : v1;
    }

    void collideVertically(Box other) {
        if (this.mass == Float.POSITIVE_INFINITY && other.mass == Float.POSITIVE_INFINITY) {
            float originalThisVY = this.velocity.y;
            this.velocity.y = other.velocity.y;
            other.velocity.y = originalThisVY;
        } else if (this.mass == Float.POSITIVE_INFINITY)
            other.velocity.y = 2 * this.velocity.y - other.velocity.y;
        else if (other.mass == Float.POSITIVE_INFINITY)
            this.velocity.y = 2 * other.velocity.y - this.velocity.y;
        else {
            float optimizedP = 2.0f * (this.velocity.y - other.velocity.y) / (this.mass + other.mass);
            this.velocity.y -= optimizedP * other.mass;
            other.velocity.y += optimizedP * this.mass;
        }
    }

    void collideHorizontally(Box other) {
        if (this.mass == Float.POSITIVE_INFINITY && other.mass == Float.POSITIVE_INFINITY) {
            float originalThisVX = this.velocity.x;
            this.velocity.x = other.velocity.x;
            other.velocity.x = originalThisVX;
        } else if (this.mass == Float.POSITIVE_INFINITY)
            other.velocity.x = 2 * this.velocity.x - other.velocity.x;
        else if (other.mass == Float.POSITIVE_INFINITY)
            this.velocity.x = 2 * other.velocity.x - this.velocity.x;
        else {
            float optimizedP = 2.0f * (this.velocity.x - other.velocity.x) / (this.mass + other.mass);
            this.velocity.x -= optimizedP * other.mass;
            other.velocity.x += optimizedP * this.mass;
        }
    }

    public void moveTill(float time) {
        position.add(PVector.mult(velocity, time));
    }

    boolean isTouching(Box other) {
        return this.position.x  - this.halfDims.x <= other.position.x + other.halfDims.x &&
                this.position.x + this.halfDims.x >= other.position.x - other.halfDims.x &&
                this.position.y - this.halfDims.y <= other.position.y + other.halfDims.y &&
                this.position.y + this.halfDims.y >= other.position.y - other.halfDims.y;
    }

    public boolean isTouching(ArrayList<Box> others) {
        for (Box other : others)
            if (isTouching(other))
                return true;
        return false;
    }


    private boolean isTouchingLeftWall() {
        return position.x - halfDims.x <= 0;
    }

    private boolean isTouchingRightWall(float width) {
        return position.x + halfDims.x >= width;
    }

    private boolean isTouchingTopWall() {
        return position.y - halfDims.y <= 0;
    }

    private boolean isTouchingBottomWall(float height) {
        return position.y + halfDims.y >= height;
    }

    public boolean isTouchingAnyWall(float width, float height) {
        return isTouchingLeftWall() || isTouchingRightWall(width) ||
                isTouchingTopWall() || isTouchingBottomWall(height);
    }

    // Walls should actually be considered as four independent infinite
    // mass objects. We're simplifying this, since our purpose with this
    // code is mainly to talk about the collision of boxes with other boxes:
    public float nextCollisionWithWall(float width, float height) {
        float nextCollisionWithVerticalWall =
                min(nextCollisionWithLeftWall(), nextCollisionWithRightWall(width));
        float nextCollisionWithHorizontalWall =
                min(nextCollisionWithTopWall(), nextCollisionWithBottomWall(height));
        verticalWallBounce =
                nextCollisionWithVerticalWall <= nextCollisionWithHorizontalWall;
        return verticalWallBounce ?
                nextCollisionWithVerticalWall :
                nextCollisionWithHorizontalWall;
    }

    private float nextCollisionWithLeftWall() {
        if (velocity.x >= 0)
            return Float.POSITIVE_INFINITY;
        return currentTime - (position.x - halfDims.x) / velocity.x;
    }

    private float nextCollisionWithRightWall(float width) {
        if (velocity.x <= 0)
            return Float.POSITIVE_INFINITY;
        return currentTime + (width - (position.x + halfDims.x)) / velocity.x;
    }

    private float nextCollisionWithTopWall() {
        if (velocity.y >= 0)
            return Float.POSITIVE_INFINITY;
        return currentTime - (position.y - halfDims.y) / velocity.y;
    }

    private float nextCollisionWithBottomWall(float height) {
        if (velocity.y <= 0)
            return Float.POSITIVE_INFINITY;
        return currentTime + (height - (position.y + halfDims.y)) / velocity.y;
    }

    public void collideWithWall() {
        if (verticalWallBounce)
            velocity.x = -velocity.x;
        else
            velocity.y = -velocity.y;
    }

    public void display(PGraphics g) {
        g.pushMatrix();
        g.pushStyle();

        g.translate(position.x, position.y);

        if (mass == Float.POSITIVE_INFINITY) {
            if (velocity.mag() == 0.0) {
                g.fill(0xffff0000);
                g.stroke(0xffff0000);
            } else {
                g.fill(0xff00ff00);
                g.stroke(0xff00ff00);
            }
        } else {
            g.fill(PApplet.map(mass, 500, 5000, 192, 0));
            g.stroke(0xffffffff);
        }

        g.rectMode(CENTER);
        g.rect(0, 0, dimensions.x, dimensions.y);
        g.rectMode(CORNER);

        g.popStyle();
        g.popMatrix();
    }
}
