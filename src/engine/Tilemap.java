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

        float level_width = mapWidth * tile_size.x;
        float level_height = mapHeight * tile_size.y;
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
            PVector collision_pos = new PVector(col * tileWidth + tileWidth / 2,row * tileHeight + tileHeight /2);
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
        map.draw(g, 0 ,0);
    }

    public boolean collideWithAgent(Agent agent) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        if (checkPitCollision(agent.position.x, agent.position.y)) {
            agent.damageLethal(100);
            agent.score -= 1;
        }

        checkTileCollisions(collision_positions, agent.position, agent.radius);

        if (collision_positions.size() == 0)
            return false;

        for (PVector pos : collision_positions)
            agent.collideWithTile(pos, tileWidth, tileHeight);

        return true;
    }

    private boolean checkPitCollision(float x, float y) {
        int col = PApplet.floor(x / tileWidth);
        int row = PApplet.floor(y / tileHeight);

        // Don't collide if outside world
        if (col < 0 || col >= mapWidth || row < 0 || row >= mapHeight) {
            return true;
        }

        if (pits == null) return false;
        for (Ptmx.CollisionShape shape : pits) {
            if (Collision.pointRect(x, y, shape.x, shape.y, shape.width, shape.height))
                return true;
        }

        return false;
    }
}
