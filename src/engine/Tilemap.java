package engine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.StringDict;

import java.util.ArrayList;

import static processing.core.PConstants.CORNER;

public class Tilemap {
    public float mapWidth;
    public float mapHeight;
    public float tileWidth;
    public float tileHeight;
    public Ptmx map;
    ArrayList<Ptmx.CollisionShape> shapes;

    public Tilemap(PApplet applet, String tile_map) {
        // Load Map
        map = new Ptmx(applet, tile_map);
        map.setDrawMode(CORNER);
        map.setPositionMode("CANVAS");

        // Get Map Info
        PVector map_size = map.getMapSize();
        PVector tile_size = map.getTileSize();
        boolean visible = map.getVisible(0);
        StringDict[] objects = map.getObjects(1);
        int objects_color = map.getObjectsColor(1);
        int[] layer_data = map.getData(0);


        shapes = map.getShapes(0);
        mapWidth = map_size.x;
        mapHeight = map_size.y;
        tileWidth = tile_size.x;
        tileHeight = tile_size.y;
    }

    public void update(float delta_time) {
        map.update(delta_time);
    }

    public void checkTileCollisions(ArrayList<PVector> collision_positions, PVector position, float radius) {
        // Top Left, Top Right, Bottom Left, Bottom Right
        checkTileCollision(collision_positions, position.x - radius, position.y - radius);
        checkTileCollision(collision_positions, position.x + radius, position.y - radius);
        checkTileCollision(collision_positions, position.x - radius, position.y + radius);
        checkTileCollision(collision_positions, position.x + radius, position.y + radius);
    }

    public void checkTileCollision(ArrayList< PVector > collision_positions, float x, float y) {
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

    public void display(PGraphics g) {
        map.draw(g, 0 ,0);
        g.pushStyle();
        g.stroke(0xFFFFFFF);
        g.strokeWeight(2);
        g.fill(0xFFFFFFFF, 128);
        for(Ptmx.CollisionShape s : shapes) {
            s.display(g);
        }
        g.popStyle();
    }

    public boolean collideWithAgent(Agent agent) {
        ArrayList<PVector> collision_positions = new ArrayList<>(4);

        // Top Left, Top Right, Bottom Left, Bottom Right
        checkTileCollision(collision_positions, agent.position.x - agent.radius, agent.position.y - agent.radius);
        checkTileCollision(collision_positions, agent.position.x + agent.radius, agent.position.y - agent.radius);
        checkTileCollision(collision_positions, agent.position.x - agent.radius, agent.position.y + agent.radius);
        checkTileCollision(collision_positions, agent.position.x + agent.radius, agent.position.y + agent.radius);
        if (collision_positions.size() == 0)
            return false;

        for (PVector pos : collision_positions)
            agent.collideWithTile(pos, tileWidth, tileHeight);

        return true;
    }
}
