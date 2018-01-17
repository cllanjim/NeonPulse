package engine;

import processing.core.PApplet;
import processing.core.PVector;

// TODO: Make functions return collision depth?
// TODO: circleRect
public final class Collision {
    private Collision() {}

    // Circular Collision
    public static boolean circular(PVector position_a, float radius_a, PVector position_b, float radius_b) {
        float collision_distance = radius_a + radius_b;
        float distance = PVector.dist(position_a, position_b);
        float collision_depth = collision_distance - distance;
        return (collision_depth > 0);
    }

    // Directional Circular Collision
    public static boolean arcCircle(PVector position_a, float radius_a, float angle, float range, PVector position_b, float radius_b) {
        float heading = PVector.sub(position_b, position_a).heading();
        float angle_diff = PApplet.abs(angle - heading);
        float collision_distance = radius_a + radius_b;
        float distance = PVector.dist(position_a, position_b);
        float collision_depth = collision_distance - distance;
        return (collision_depth > 0 && angle_diff < range / 2);
    }

    // Axis-Aligned Boundary Box Collision
    public static boolean AABB(PVector position_a, PVector dimensions_a, PVector position_b, PVector dimensions_b) {
        return AABB(position_a, dimensions_a.x, dimensions_a.y, position_b, dimensions_b.x, dimensions_b.y);
    }

    public static boolean AABB(PVector position_a, float width_a, float height_a, PVector position_b, float width_b, float height_b) {
        float collision_distance_x = width_a / 2 + width_b / 2;
        float collision_distance_y = height_a / 2 + height_b / 2;
        float center_x = position_a.x + width_a / 2;
        float center_y = position_a.y + height_a / 2;
        float offset_x = center_x - position_b.x;
        float offset_y = center_y - position_b.y;
        float collision_depth_x = collision_distance_x - PApplet.abs(offset_x);
        float collision_depth_y = collision_distance_y - PApplet.abs(offset_y);
        return (collision_depth_x > 0) && (collision_depth_y > 0);
    }

    // Line-Circle Collision
    public static boolean lineCircle(PVector line_start, PVector line_end, PVector circle_position, float circle_radius) {
        PVector line_vector = PVector.sub(line_end, line_start);
        PVector circle_vector = PVector.sub(circle_position, line_start);

        float u = (circle_vector.x * line_vector.x + circle_vector.y * line_vector.y) / line_vector.magSq();

        PVector closestPoint = (u < 0)
                ? line_start
                : (u > 1)
                    ? line_end
                    : PVector.add(line_start, PVector.mult(line_vector, u));

        return (closestPoint.dist(circle_position) < circle_radius);
    }

    // Segment - Segment Collision
    public static boolean lineSegments(PVector a_start, PVector a_end, PVector b_start, PVector b_end, PVector intersection_point) {
        PVector a_vector = PVector.sub(a_end, a_start);
        PVector b_vector = PVector.sub(b_end, b_start);

        float d = a_vector.x * b_vector.y - a_vector.y * b_vector.x;

        // No collision if lines are parallel, ie determinant = 0
        if (d == 0) return false;

        // Segment fraction will be between 0 and 1 inclusive if the lines intersect
        float n_a = b_vector.x * (a_start.y - b_start.y) - b_vector.y * (a_start.x - b_start.x);
        float n_b = a_vector.x * (a_start.y - b_start.y) - a_vector.y * (a_start.x - b_start.x);
        float ua = n_a / d;
        float ub = n_b / d;

        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            intersection_point.set(PVector.add(a_start, PVector.mult(a_vector, ua)));
            return true;
        }
        return false;
    }

    // Point Collision
    public static boolean pointRect(float x, float y, float rect_x, float rect_y, float width, float height) {
        return (x >= rect_x) && (x <= (rect_x + width)) && (y >= rect_y) && (y <= (rect_y + height));
    }

    // Point Collision
    public static boolean pointCircle(PVector point, PVector position, float radius) {
        return PVector.sub(position, point).mag() < radius;
    }
}
