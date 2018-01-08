package extra;

import processing.core.PVector;

public abstract class GameObject {
    PVector origin;
    SpriteComponent sprite;
    CollisionComponent collision;
    PhysicsComponent physics;
}
