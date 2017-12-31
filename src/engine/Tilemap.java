package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.StringDict;
import ptmx.Ptmx;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PConstants.CORNER;

public class Tilemap {
    public float mapWidth;
    public float mapHeight;
    public float tileWidth;
    public float tileHeight;
    public float level_width;
    public float level_height;
    public float top, left;
    public Ptmx map;
    private ArrayList<Ptmx.CollisionShape> pits;
    List<Ptmx.CollisionShape> shapes;

    public Tilemap(PApplet applet, String tile_map) {
        // Load Map
        map = new Ptmx(applet, tile_map);
        map.setDrawMode(CORNER);
        map.setPositionMode("CANVAS");

        // Get Map Info
        PVector map_size = map.getMapSize();
        PVector tile_size = map.getTileSize();

        int[] layer_data = map.getData(0);
        boolean visible = map.getVisible(0);
        ArrayList<StringDict> objects = map.getObjects(1);
        int objects_color = map.getObjectsColor(1);

        shapes = map.getShapes(0);
        pits = new ArrayList<>(objects.size());
        for (StringDict obj : objects) {
            pits.add(Ptmx.CollisionShape.fromStringDict(obj));
        }

        mapWidth = map_size.x;
        mapHeight = map_size.y;
        tileWidth = tile_size.x;
        tileHeight = tile_size.y;
        level_width = mapWidth * tile_size.x;
        level_height = mapHeight * tile_size.y;

        top = (applet.height - level_height) / 2;
        left = (applet.width - level_width) / 2;
    }

    public void update(float delta_time) {
        map.update(delta_time);
    }

    public void checkTileCollisions(List<PVector> collision_positions, PVector position, float radius) {
        // Top Left, Top Right, Bottom Left, Bottom Right
        checkTileCollision(collision_positions, position.x - radius, position.y - radius);
        checkTileCollision(collision_positions, position.x + radius, position.y - radius);
        checkTileCollision(collision_positions, position.x - radius, position.y + radius);
        checkTileCollision(collision_positions, position.x + radius, position.y + radius);
    }

    public void checkTileCollision(List<PVector> collision_positions, float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);

        // Don't collide if outside world
        if (col < 0 || col >= mapWidth || row < 0 || row >= mapHeight) return;

        // TODO: Get specific tiles/references, use custom collision functions
        if (map.getLayer(0).getShapesAt(col, row) != null) {
            PVector collision_pos = new PVector(col * tileWidth + tileWidth / 2, row * tileHeight + tileHeight / 2);
            collision_positions.add(collision_pos);
        }
    }

    public boolean checkCollision(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);

        // No collision outside world, or if shapes are null
        return col >= 0 && !(col >= mapWidth) && row >= 0 && !(row >= mapHeight)
                && map.getLayer(0).getShapesAt(col, row) != null;
    }

    public void display(PGraphics g) {
        map.draw(g, 0, 0);
    }

    public boolean collideWithAgent(Agent agent) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        // Wrap if outside world
        if (agent.position.x < 0) agent.position.x = level_width;
        if (agent.position.x > level_width) agent.position.x = 0;
        if (agent.position.y < 0) agent.position.y = level_height;
        if (agent.position.y > level_height) agent.position.y = 0;

        if (checkPitCollision(agent.position.x, agent.position.y)) {
            agent.damageLethal(100);
            agent.score -= 1;
        }

        checkTileCollisions(collision_positions, agent.position, agent.radius);

        if (collision_positions.size() == 0) return false;
        for (PVector pos : collision_positions)
            agent.collideWithTile(pos, tileWidth, tileHeight);

        return true;
    }

    private boolean checkPitCollision(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);

        // Don't collide if outside world
        if (col < 0 || col >= mapWidth || row < 0 || row >= mapHeight)  return false;

        if (pits == null) return false;
        for (Ptmx.CollisionShape shape : pits) {
            if (Collision.pointRect(x, y, shape.x, shape.y, shape.width, shape.height))
                return true;
        }

        return false;
    }

    void getPoints(List<PVector> points, PVector p0, PVector p1) {
        float dx = p1.x - p0.x;
        float dy = p1.y - p0.y;
        float nx = Math.abs(dx);
        float ny = Math.abs(dy);
        float sign_x = dx > 0 ? 1 : -1;
        float sign_y = dy > 0 ? 1 : -1;

        PVector p = new PVector(p0.x, p0.y);
        points.add(new PVector(p.x, p.y));

        for (int ix = 0, iy = 0; ix < nx || iy < ny; ) {
            if ((0.5 + ix) / nx == (0.5 + iy) / ny) {
                // next step is diagonal
                p.x += sign_x;
                p.y += sign_y;
                ix++;
                iy++;
            } else if ((0.5 + ix) / nx < (0.5 + iy) / ny) {
                // next step is horizontal
                p.x += sign_x;
                ix++;
            } else {
                // next step is vertical
                p.y += sign_y;
                iy++;
            }
            points.add(new PVector(p.x, p.y));
        }
    }

    void getPoints(List<PVector> points, int y1, int x1, int y2, int x2) {
        int i;               // loop counter
        int ystep, xstep;    // the step on y and x axis
        int y = y1, x = x1;  // the line points
        int ddy, ddx;        // compulsory variables: the double values of dy and dx

        int dx = x2 - x1;
        int dy = y2 - y1;

        points.add(new PVector(y1, x1));  // first point

        // NB the last point can't be here, because of its previous point (which has to be verified)

        if (dy < 0) {
            ystep = -1;
            dy = -dy;
        } else {
            ystep = 1;
        }

        if (dx < 0) {
            xstep = -1;
            dx = -dx;
        } else {
            xstep = 1;
        }

        ddy = 2 * dy;
        ddx = 2 * dx;

        if (ddx >= ddy) {  // first octant (0 <= slope <= 1)
            // compulsory initialization (even for errorprev, needed when dx==dy)
            int errorprev = dx;
            int error = dx;  // start in the middle of the square
            for (i = 0; i < dx; i++) {  // do not use the first point (already done)
                x += xstep;
                error += ddy;
                if (error > ddx) {  // increment y if AFTER the middle ( > )
                    y += ystep;
                    error -= ddx;
                    // three cases (octant == right->right-top for directions below):
                    if (error + errorprev < ddx)  // bottom square also
                        points.add(new PVector(y - ystep, x));
                    else if (error + errorprev > ddx)  // left square also
                        points.add(new PVector(y, x - xstep));
                    else {  // corner: bottom and left squares also
                        points.add(new PVector(y - ystep, x));
                        points.add(new PVector(y, x - xstep));
                    }
                }
                points.add(new PVector(y, x));
                errorprev = error;
            }
        } else {  // the same as above
            int errorprev = dy;
            int error = dy;
            for (i = 0; i < dy; i++) {
                y += ystep;
                error += ddx;
                if (error > ddy) {
                    x += xstep;
                    error -= ddy;
                    if (error + errorprev < ddy)
                        points.add(new PVector(y, x - xstep));
                    else if (error + errorprev > ddy)
                        points.add(new PVector(y - ystep, x));
                    else {
                        points.add(new PVector(y, x - xstep));
                        points.add(new PVector(y - ystep, x));
                    }
                }
                points.add(new PVector(y, x));
                errorprev = error;
            }
        }
        assert((y == y2) && (x == x2));  // the last point (y2,x2) has to be the same with the last point of the algorithm
    }
}
