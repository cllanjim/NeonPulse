package util;

import processing.core.PVector;

class CenterParticle extends Particle {
    private final PVector center;

    CenterParticle(PVector origin, float life_span, PVector velocity) {
        this(origin, life_span, velocity.heading(), velocity.mag());
    }

    CenterParticle(PVector origin, float life_span, float angle, float speed) {
        super(origin, life_span, angle, speed);
        PVector offset = PVector.fromAngle(angle).setMag(speed);
        this.center = origin.copy();
        this.position.set(origin.x + offset.x, origin.y + offset.y);
        this.velocity.set(0.5f * (origin.x - position.x), 0.5f * (origin.y - position.y));
    }

    void update(float delta_time) {
        super.update(delta_time);
        if (PVector.dist(position, center) < 5)
            lifespan = 0;
    }
}