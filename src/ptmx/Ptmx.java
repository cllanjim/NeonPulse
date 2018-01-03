package ptmx;

import processing.core.*;
import processing.data.*;
import util.MultiMap;

import java.util.ArrayList;
import java.util.HashMap;

import static processing.core.PApplet.*;

public class Ptmx {
    private PApplet parent;
    private float version;
    private String filename, orientation, renderorder, staggeraxis;
    private int camwidth, camheight, camleft, camtop;
    private int mapwidth, mapheight;
    private int tilewidth, tileheight, staggerindex, hexsidelength, backgroundcolor;
    private int drawmargin = 2;
    private int drawmode = PConstants.CORNER;
    private String positionmode = "CANVAS";//"CANVAS" or "MAP"
    private HashMap<Integer, Tile> tile_map = new HashMap<>();
    private MultiMap<Integer, CollisionShape> collision_map = new MultiMap<>();
    private ArrayList<Layer> layers = new ArrayList<>();
    private Xmap map;
    private int timer;

    abstract class Xmap {
        abstract PVector mapToCanvas(float nx, float ny);
        abstract PVector canvasToMap(float x, float y);
        abstract void drawTileLayer(PGraphics pg, Layer l);

        void drawTileLayer(PGraphics pg, Layer l, int xstart, int xstop, int ystart, int ystop) {
            for (int ny = ystart; ny < ystop; ny++) {
                for (int nx = xstart; nx < xstop; nx++) {
                    int n = l.data[nx + ny * Ptmx.this.mapwidth] - 1;
                    if (n >= 0) {
                        Tile tile = Ptmx.this.tile_map.get(n);
                        if (tile == null) break;
                        tile.draw(pg, l, nx, ny);
                    }
                }
            }
        }
    }

    private class OrthogonalMap extends Xmap {
        public PVector mapToCanvas(float nx, float ny) {
            float x = (nx + 0.5f) * Ptmx.this.tilewidth;
            float y = (ny + 0.5f) * Ptmx.this.tileheight;
            return new PVector(x, y);
        }

        public PVector canvasToMap(float x, float y) {
            float nx = x / Ptmx.this.tilewidth - 0.5f;
            float ny = y / Ptmx.this.tileheight - 0.5f;
            return new PVector(nx, ny);
        }

        public void drawTileLayer(PGraphics pg, Layer l) {
            int xstart = max(0, floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin));
            int xstop = min(Ptmx.this.mapwidth, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
            int ystart = max(0, floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
            int ystop = min(Ptmx.this.mapheight, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
            drawTileLayer(pg, l, xstart, xstop, ystart, ystop);
        }
    }

    private class IsometricMap extends Xmap {
        public PVector mapToCanvas(float nx, float ny) {
            float x = (Ptmx.this.mapwidth + Ptmx.this.mapheight) * Ptmx.this.tilewidth / 4 + (nx - ny) * Ptmx.this.tilewidth / 2;
            float y = (nx + ny + 1) * Ptmx.this.tileheight / 2;
            return new PVector(x, y);
        }

        public PVector canvasToMap(float x, float y) {
            float dif = (Ptmx.this.mapwidth + Ptmx.this.mapheight) * Ptmx.this.tilewidth / 4;
            float nx = y / Ptmx.this.tileheight + ((x - dif) / Ptmx.this.tilewidth) - 0.5f;
            float ny = y / Ptmx.this.tileheight - ((x - dif) / Ptmx.this.tilewidth) - 0.5f;
            return new PVector(nx, ny);
        }

        public void drawTileLayer(PGraphics pg, Layer l) {
            int xstart = max(0, floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin));
            int xstop = min(Ptmx.this.mapwidth, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
            int ystart = max(0, floor(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
            int ystop = min(Ptmx.this.mapheight, ceil(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
            drawTileLayer(pg, l, xstart, xstop, ystart, ystop);
        }
    }

    private class StaggeredXMap extends Xmap {
        public PVector mapToCanvas(float nx, float ny) {
            float x = (nx * (Ptmx.this.hexsidelength + Ptmx.this.tilewidth) + Ptmx.this.tilewidth) / 2;
            float y = (ny + 0.5f + Ptmx.this.staggerindex / 2) * Ptmx.this.tileheight + (Ptmx.this.staggerindex * 2 - 1) * (abs(abs(nx) % 2 - 1) - 1) * (Ptmx.this.tileheight) / 2;
            return new PVector(x, y);
        }

        public PVector canvasToMap(float x, float y) {
            float nx = (x * 2 - Ptmx.this.tilewidth) / (Ptmx.this.hexsidelength + Ptmx.this.tilewidth);
            float ny = (Ptmx.this.staggerindex == 0)
                    ? y / Ptmx.this.tileheight - 1.0f + (abs(abs(nx) % 2 - 1)) / 2
                    : y / Ptmx.this.tileheight - 0.5f - (abs(abs(nx) % 2 - 1)) / 2;
            return new PVector(nx, ny);
        }

        public void drawTileLayer(PGraphics pg, Layer l) {
            int xstart = floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin);
            xstart = max(0, xstart - xstart % 2);
            int xstop = min(Ptmx.this.mapwidth, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
            int ystart = max(0, floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
            int ystop = min(Ptmx.this.mapheight, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
            drawTileLayer(pg, l, xstart, xstop, ystart, ystop);
        }
    }

    private class StaggeredYMap extends Xmap {
        public PVector mapToCanvas(float nx, float ny) {
            float y = ny * (Ptmx.this.hexsidelength + Ptmx.this.tileheight) / 2 + Ptmx.this.tileheight / 2;
            float x = (nx + 0.5f + Ptmx.this.staggerindex / 2) * Ptmx.this.tilewidth
                    + (Ptmx.this.staggerindex * 2 - 1) * (abs(abs(y) % 2 - 1) - 1) * (Ptmx.this.tilewidth) / 2;
            return new PVector(x, y);
        }

        public PVector canvasToMap(float x, float y) {
            float ny = (y * 2 - Ptmx.this.tileheight) / (Ptmx.this.hexsidelength + Ptmx.this.tileheight);
            float nx = (Ptmx.this.staggerindex == 0)
                    ? x / Ptmx.this.tilewidth - 1.0f + (abs(abs(ny) % 2 - 1)) / 2
                    : x / Ptmx.this.tilewidth - 0.5f - (abs(abs(ny) % 2 - 1)) / 2;
            return new PVector(nx, ny);
        }

        public void drawTileLayer(PGraphics pg, Layer l) {
            int xstart = floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin);
            xstart = max(0, xstart - xstart % 2);
            int xstop = min(Ptmx.this.mapwidth, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
            int ystop = min(Ptmx.this.mapheight, ceil(canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
            int ystart = max(0, floor(canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
            drawTileLayer(pg, l, xstart, xstop, ystart, ystop);
        }
    }

    private class Tile {
        final PImage image;
        final int offsetx, offsety;

        Tile(PImage image, int offsetx, int offsety) {
            this.image = image;
            this.offsetx = offsetx;
            this.offsety = offsety;
        }

        protected void draw(PGraphics pg, Layer l, int nx, int ny) {
            PVector p = Ptmx.this.mapToCam(nx, ny);
            float x = p.x - Ptmx.this.tilewidth / 2 + offsetx + l.offsetx;
            float y = p.y - Ptmx.this.tileheight / 2 + offsety + l.offsety + (Ptmx.this.tileheight - image.height);
            pg.image(image, x, y);
        }
    }

    private class AnimatedTile extends Tile {
        private ArrayList<Frame> frames;
        private int animationDuration;

        AnimatedTile(PImage image, int offsetx, int offsety) {
            super(image, offsetx, offsety);
            this.frames = new ArrayList<>();
            this.animationDuration = 0;
        }

        void addFrame(int tileid, int frame_duration) {
            this.frames.add(new Frame(tileid, frame_duration, animationDuration + frame_duration));
            animationDuration += frame_duration;
        }

        protected void draw(PGraphics pg, Layer l, int nx, int ny) {
            PVector p = Ptmx.this.mapToCam(nx, ny);
            float x = p.x - Ptmx.this.tilewidth / 2 + offsetx + l.offsetx;
            float y = p.y - Ptmx.this.tileheight / 2 + offsety + l.offsety + (Ptmx.this.tileheight - image.height);

            int animation_cursor = Ptmx.this.timer % animationDuration;
            int frame_tileid = 0;
            for (Frame f : frames) {
                frame_tileid = f.tileid;
                if (animation_cursor < f.cumulative) break;
            }

            Tile frame_tile = Ptmx.this.tile_map.get(frame_tileid);
            if (frame_tile != null) {
                pg.image(frame_tile.image, x, y);
            }
        }
    }

    private class Frame {
        final int tileid;
        final int duration;
        final int cumulative;

        Frame(int tileid, int duration, int cumulative) {
            this.tileid = tileid;
            this.duration = duration;
            this.cumulative = cumulative;
        }
    }

    // TODO: Defaults and warnings for missing values, with error message pointing to tsx/tmx
    public abstract static class CollisionShape {
        int id;
        String type;
        public float x;
        public float y;
        public float width;
        public float height;
        PVector[] points;

        CollisionShape(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        static CollisionShape fromXML(XML o) {
            int id = parseInt(o.getString("id"));
            float x = parseInt(o.getString("x"));
            float y = parseInt(o.getString("y"));
            if (o.getChild("polyline") != null) {
                return new Polyline(id, x, y, readPoints(o.getChild("polyline").getString("points")));
            } else if (o.getChild("polygon") != null) {
                return new Polygon(id, x, y, readPoints(o.getChild("polygon").getString("points")));
            } else if (o.getChild("ellipse") != null) {
                return new Ellipse(id, x, y, parseInt(o.getString("width")), parseInt(o.getString("height")));
            } else {
                return new Rectangle(id, x, y, parseInt(o.getString("width")), parseInt(o.getString("height")));
            }
        }

        public static CollisionShape fromStringDict(StringDict o) {
            int id = parseInt(o.get("id"));
            float x = parseInt(o.get("x"));
            float y = parseInt(o.get("y"));
            String object = o.get("object");
            switch (object) {
                case "rectangle":
                    return new Rectangle(id, x, y, parseInt(o.get("width")), parseInt(o.get("height")));
                case "ellipse":
                    return new Ellipse(id, x, y, parseInt(o.get("width")), parseInt(o.get("height")));
                case "polygon":
                    return new Polygon(id, x, y, readPoints(o.get("points")));
                case "polyline":
                    return new Polyline(id, x, y, readPoints(o.get("points")));
            }
            return null;
        }

        public abstract void display(PGraphics g);
    }

    private static class Rectangle extends CollisionShape {
        Rectangle(int id, float x, float y, float width, float height) {
            super(id, x, y);
            type = "rectangle";
            this.width = width;
            this.height = height;
            this.points = getPointsFromCorner(x, y, width, height);
        }

        @Override
        public void display(PGraphics g) {
            g.rect(x, y, width, height);
        }
    }

    private static class Ellipse extends CollisionShape {
        Ellipse(int id, float x, float y, float width, float height) {
            super(id, x, y);
            type = "ellipse";
            this.width = width;
            this.height = height;
            this.points = getPointsFromCorner(x, y, width, height);
        }

        @Override
        public void display(PGraphics g) {
            g.ellipse(x, y, width, height);
        }
    }

    private static class Polygon extends CollisionShape {
        Polygon(int id, float x, float y, PVector[] points) {
            super(id, x, y);
            this.points = points;
            type = "polygon";
        }

        @Override
        public void display(PGraphics pg) {
            pg.beginShape();
            for (PVector p : points) {
                pg.vertex(x + p.x, y + p.y);
            }
            pg.endShape(CLOSE);
        }
    }

    private static class Polyline extends CollisionShape {
        Polyline(int id, float x, float y, PVector[] points) {
            super(id, x, y);
            type = "polyline";
            this.points = points;
        }

        @Override
        public void display(PGraphics pg) {
            pg.pushStyle();
            pg.fill(0xFF007F00);
            pg.stroke(0xFF7F0000);
            pg.beginShape();
            for (PVector p : points) {
                pg.vertex(x + p.x, y + p.y);
            }
            pg.endShape();
            pg.popStyle();
        }
    }

    private static PVector[] readPoints(String points) {
        String[] pointStrings = points.split(" ");
        PVector[] pointsArray = new PVector[pointStrings.length];
        for (int i = 0; i < pointStrings.length; i++) {
            String[] xy = pointStrings[i].split(",");
            pointsArray[i] = new PVector(parseFloat(xy[0]), parseFloat(xy[1]));
        }
        return pointsArray;
    }

    private static PVector[] getPointsFromCorner(float x, float y, float width, float height) {
        return new PVector[] {
                new PVector(x, y),
                new PVector(x + width, y),
                new PVector(x + width, y + height),
                new PVector(x, y + height)
        };
    }

    private static PVector[] getPointsFromCenter(float x, float y, float width, float height) {
        return new PVector[] {
                new PVector(x - width/2, y - height / 2),
                new PVector(x + width/2, y - height / 2),
                new PVector(x + width/2, y + height / 2),
                new PVector(x - width/2, y + height / 2),
        };
    }

    private static Rectangle makeAABB(float x, float y, PVector[] points) {
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < points.length; i++) {
            minX = Math.min(minX, points[i].x);
            maxX = Math.max(maxX, points[i].x);
            minY = Math.min(minY, points[i].y);
            maxY = Math.max(maxY, points[i].y);
        }

        float width = maxX - minX;
        float height = maxY - minY;
        
        return new Rectangle(0, x + minX, y + minY, width, height);
    }

    public class Layer {
        private String type, name;
        private boolean visible;
        private float offsetx, offsety, opacity;
        private int objcolor;
        private int[] data;
        private PImage image;
        private ArrayList<StringDict> objects;
        private ArrayList<CollisionShape> shapes;
        private StringDict properties;

        Layer(XML e) {
            type = "layer";
            name = e.getString("name");
            visible = !e.hasAttribute("visible") || e.getInt("visible") == 1;
            offsetx = e.hasAttribute("offsetx") ? e.getFloat("offsetx") : 0;
            offsety = e.hasAttribute("offsety") ? e.getFloat("offsety") : 0;
            opacity = e.hasAttribute("opacity") ? e.getFloat("opacity") : 1;
            objcolor = e.hasAttribute("color")
                    ? readColor(e.getString("color"))
                    : parent.color(0);
            objects = new ArrayList<>();
            shapes = new ArrayList<>();
            properties = new StringDict();
            if (e.getChild("properties") != null) {
                XML props[] = e.getChild("properties").getChildren("property");
                for (XML p : props) properties.set(p.getString("name"), p.getString("value"));
            }

            switch (e.getName()) {
                case "layer":
                    type = "layer";
                    loadTileLayer(e);
                    break;
                case "imagelayer":
                    type = "imagelayer";
                    loadImageLayer(e);
                    break;
                case "objectgroup":
                    type = "objectgroup";
                    loadObjectLayer(e);
                    break;
            }
        }

        private void loadTileLayer(XML e) {
            XML c = e.getChild("data");
            if (!c.getString("encoding").equals("csv"))
                throw new RuntimeException("Tmx can only handle CSV encoding");
            data = parseInt(split(c.getContent().replace("\n", ""), ","));

            for (int i = 0; i < data.length; i++) {
                int row = i / Ptmx.this.mapwidth;
                int col = i % Ptmx.this.mapwidth;
                float top = row * tileheight + offsety;
                float left = col * tilewidth + offsetx;

                ArrayList<CollisionShape> tile_shapes = Ptmx.this.collision_map.get(data[i]);
                if (tile_shapes != null) {
                    for (CollisionShape s : tile_shapes) {
                        switch (s.type) {
                            case "rectangle":
                                shapes.add(new Rectangle(s.id, s.x + left, s.y + top, s.width, s.height));
                                break;
                            case "ellipse":
                                shapes.add(new Ellipse(s.id, s.x + left, s.y + top, s.width, s.height));
                                break;
                            case "polygon":
                                shapes.add(new Polygon(s.id, s.x + left, s.y + top, s.points));
                                break;
                            case "polyline":
                                shapes.add(new Polyline(s.id, s.x + left, s.y + top, s.points));
                                break;
                        }
                    }
                }
            }
        }

        private void loadImageLayer(XML e) {
            XML c = e.getChild("image");
            image = parent.loadImage(c.getString("source"));
            if (c.hasAttribute("trans")) Ptmx.this.applyTransColor(image, c.getString("trans"));
        }

        private void loadObjectLayer(XML e) {
            XML xo[] = e.getChildren("object");
            for (XML o : xo) {
                StringDict prop = new StringDict();
                if (o.getChild("ellipse") != null) {
                    prop.set("object", "ellipse");
                } else if (o.getChild("polyline") != null) {
                    prop.set("object", "polyline");
                    prop.set("points", o.getChild("polyline").getString("points"));
                } else if (o.getChild("polygon") != null) {
                    prop.set("object", "polygon");
                    prop.set("points", o.getChild("polygon").getString("points"));
                } else {
                    prop.set("object", "rectangle");
                }
                if (o.hasAttribute("id")) prop.set("id", o.getString("id"));
                if (o.hasAttribute("name")) prop.set("name", o.getString("name"));
                if (o.hasAttribute("type")) prop.set("type", o.getString("type"));
                if (o.hasAttribute("visible")) prop.set("visible", o.getString("visible"));
                if (o.hasAttribute("x")) prop.set("x", o.getString("x"));
                if (o.hasAttribute("y")) prop.set("y", o.getString("y"));
                if (o.hasAttribute("width")) prop.set("width", o.getString("width"));
                if (o.hasAttribute("height")) prop.set("height", o.getString("height"));
                if (o.hasAttribute("rotation")) prop.set("rotation", o.getString("rotation"));
                if (o.getChild("properties") != null) {
                    XML childs[] = o.getChild("properties").getChildren("property");
                    for (XML p : childs) prop.set(p.getString("name"), p.getString("value"));
                }
                objects.add(prop);
                shapes.add(CollisionShape.fromStringDict(prop));
            }
        }

        public ArrayList<CollisionShape> getShapes() {
            return shapes;
        }

        public ArrayList<CollisionShape> getShapesAt(int x, int y) {
            int tile_index = y * mapwidth + x;
            if (tile_index > data.length) return null;
            return Ptmx.this.collision_map.get(data[tile_index]);
        }
    }

    public Ptmx(PApplet p, String filename) {
        this.parent = p;
        this.camwidth = p.width;
        this.camheight = p.height;
        this.filename = filename;

        XML xml = parent.loadXML(filename);
        if (xml == null) {
            throw new RuntimeException("Not a valid XML File or not UTF-8 encoded");
        }
        if (!xml.getName().equals("map")) {
            throw new RuntimeException("Not a Tmx file (missing 'map' element)");
        }

        loadTmxProperties(xml);

        switch (this.orientation) {
            case "orthogonal":
                map = new OrthogonalMap();
                break;
            case "isometric":
                map = new IsometricMap();
                break;
            case "staggered":
                this.hexsidelength = 0;
            case "hexagonal":
                map = (this.staggeraxis.equals("x")) ? new StaggeredXMap() : new StaggeredYMap();
                break;
        }

        XML children[] = xml.getChildren("tileset");
        for (XML e : children) {
            int first = e.getInt("firstgid");
            loadTileset(e, first);
        }

        children = xml.getChildren();
        for (XML e : children)
            switch (e.getName()) {
                case "layer":
                case "imagelayer":
                case "objectgroup":
                    layers.add(new Layer(e));
                    break;
            }
    }

    private void loadTmxProperties(XML e) {
        this.version = e.getFloat("version");
        this.orientation = e.getString("orientation");
        this.renderorder = e.getString("renderorder");
        this.mapwidth = e.getInt("width");
        this.mapheight = e.getInt("height");
        this.tilewidth = e.getInt("tilewidth");
        this.tileheight = e.getInt("tileheight");
        this.staggerindex = e.hasAttribute("staggerindex") ? e.getString("staggerindex").equals("even") ? 1 : 0 : 0;
        this.staggeraxis = e.hasAttribute("staggeraxis") ? e.getString("staggeraxis") : "x";
        this.hexsidelength = e.hasAttribute("hexsidelength") ? e.getInt("hexsidelength") : 0;
        this.backgroundcolor = e.hasAttribute("backgroundcolor") ? readColor(e.getString("backgroundcolor")) : 0x808080;
    }

    private void loadTileset(XML e, int firstgid) {
        if (e.hasAttribute("source") && e.getChild("image") == null) {
            loadTileset(parent.loadXML(e.getString("source")), firstgid);
            return;
        }
        int tilewidth = e.getInt("tilewidth");
        int tileheight = e.getInt("tileheight");
        int tilecount = e.hasAttribute("tilecount") ? e.getInt("tilecount") : 0;
        int columns = e.hasAttribute("columns") ? e.getInt("columns") : 0;
        int spacing = e.hasAttribute("spacing") ? e.getInt("spacing") : 0;
        int margin = e.hasAttribute("margin") ? e.getInt("margin") : 0;
        int tileoffsetx = 0;
        int tileoffsety = 0;

        XML c = e.getChild("tileoffset");
        if (c != null) {
            tileoffsetx = c.getInt("x");
            tileoffsety = c.getInt("y");
        }
        c = e.getChild("image");
        if (c != null) {
            PImage source = parent.loadImage(c.getString("source"));
            if (columns == 0) columns = floor((source.width - margin) / (tilewidth + spacing));
            if (tilecount == 0) tilecount = columns * floor((source.height - margin) / (tileheight + spacing));
            if (c.hasAttribute("trans")) this.applyTransColor(source, c.getString("trans"));
            for (int n = 0; n < tilecount; n++) {
                PImage image = source.get(
                        margin + (n % columns) * (tilewidth + spacing),
                        margin + floor(n / columns) * (tileheight + spacing),
                        tilewidth,
                        tileheight);
                this.tile_map.put(c.getInt("id") + firstgid - 1, new Tile(image, tileoffsetx, tileoffsety));
            }
        } else {
            XML tiles[] = e.getChildren("tile");
            if (tiles != null) {
                for (XML t : tiles) {
                    int tileIndex = t.getInt("id") + firstgid - 1;
                    PImage image = parent.loadImage(t.getChild("image").getString("source"));

                    // Animation
                    XML animation = t.getChild("animation");
                    if (animation != null) {
                        XML frames[] = animation.getChildren("frame");
                        AnimatedTile animatedTile = new AnimatedTile(image, tileoffsetx, tileoffsety);
                        if (frames != null) {
                            for (XML f : frames) {
                                int tileid = f.getInt("tileid");
                                int duration = f.getInt("duration");
                                animatedTile.addFrame(tileid + firstgid - 1, duration);
                            }
                        }
                        this.tile_map.put(tileIndex, animatedTile);
                    } else {
                        this.tile_map.put(tileIndex, new Tile(image, tileoffsetx, tileoffsety));
                    }

                    // Collisions
                    XML collisions = t.getChild("objectgroup");
                    if (collisions != null) {
                        XML objects[] = collisions.getChildren("object");
                        if (objects != null) {
                            for (XML o : objects) {
                                CollisionShape shape = CollisionShape.fromXML(o);
                                collision_map.put(tileIndex, shape);
                            }
                        }
                    }
                }
            }
        }
    }

    private static int readColor(String s) {
        if (s.length() == 7 || s.length() == 9) s = s.substring(1);
        if (s.length() == 6) s = "FF" + s;
        return unhex(s);
    }

    private void applyTransColor(PImage source, String c) {
        int trans = readColor(c);
        source.loadPixels();
        for (int p = 0; p < source.pixels.length; p++)
            if (source.pixels[p] == trans) source.pixels[p] = parent.color(255, 1);
        source.updatePixels();
    }

    private void prepareDraw(PGraphics pg, float left, float top) {
        this.camwidth = pg.width;
        this.camheight = pg.height;
        if (this.positionmode.equals("MAP")) {
            PVector p = this.mapToCanvas(left, top);
            left = p.x;
            top = p.y;
        }
        if (this.drawmode == PConstants.CENTER) {
            this.camleft = floor(left - this.camwidth / 2);
            this.camtop = floor(top - this.camheight / 2);
        } else {
            this.camleft = floor(left);
            this.camtop = floor(top);
        }
        pg.pushMatrix();
        pg.pushStyle();
        pg.imageMode(parent.CORNER);
    }

    private void finishDraw(PGraphics pg) {
        pg.popStyle();
        pg.popMatrix();
    }

    private void drawLayer(PGraphics pg, int m) {
        Layer l = this.layers.get(m);
        switch (l.type) {
            case "layer":
                map.drawTileLayer(pg, l);
                break;
            case "imagelayer":
                pg.image(l.image, -this.camleft + l.offsetx, -this.camtop + l.offsety);
                break;
            case "objectgroup":
                drawObjectLayer(pg, l);
                break;
        }
        applyOpacity(pg, l);
    }

    private void applyOpacity(PGraphics pg, Layer l) {
        if (pg != parent.g && l.opacity < 1) {
            pg.loadPixels();
            int a = parseInt(map(l.opacity, 0, 1, 0, 255));
            for (int p = 0; p < pg.pixels.length; p++)
                if (parent.alpha(pg.pixels[p]) > a)
                    pg.pixels[p] = parent.color(parent.red(pg.pixels[p]), parent.green(pg.pixels[p]), parent.blue(pg.pixels[p]), a);
            pg.updatePixels();
        }
    }

    private void drawObjectLayer(PGraphics pg, Layer l) {
        pg.fill(l.objcolor);
        pg.stroke(l.objcolor);
        pg.strokeWeight(1);
        for (StringDict o : l.objects) {
            if (!o.hasKey("visible")) {
                drawObject(pg, l, o);
            }
        }
    }

    private void drawObject(PGraphics pg, Layer l, StringDict o) {
        pg.pushMatrix();
        pg.pushStyle();
        pg.ellipseMode(CORNER);
        pg.translate(parseFloat(o.get("x")) - l.offsetx - this.camleft, parseFloat(o.get("y")) - l.offsety - this.camtop);
        float rotation = (o.hasKey("rotation"))
                ? parseFloat(o.get("rotation")) * PI / 180
                : 0;
        if (rotation != 0) pg.rotate(rotation);
        switch (o.get("object")) {
            case "rectangle":
                pg.rect(0, 0, parseFloat(o.get("width")), parseFloat(o.get("height")));
                break;
            case "ellipse":
                pg.ellipse(0, 0, parseFloat(o.get("width")), parseFloat(o.get("height")));
                break;
            case "tile":
                Tile tile = this.tile_map.get(parseInt(o.get("gid")) - 1);
                if (rotation != 0) pg.rotate(-rotation);
                pg.translate(0, -tile.image.height);
                if (rotation != 0) pg.rotate(rotation);
                pg.image(tile.image, tile.offsetx, tile.offsety, parseFloat(o.get("width")), parseFloat(o.get("height")));
                break;
            case "polygon":
            case "polyline":
                if (o.get("object").equals("polyline")) pg.noFill();
                pg.beginShape();
                for (String s : split(o.get("points"), " ")) {
                    float[] p = parseFloat(split(s, ","));
                    pg.vertex(p[0], p[1]);
                }
                if (o.get("object").equals("polyline")) pg.endShape();
                else pg.endShape(parent.CLOSE);
                break;
        }
        pg.popStyle();
        pg.popMatrix();
    }

    public void update(float delta_time) {
        this.timer += delta_time;
    }

    public void draw() {
        this.draw(parent.g, 0, 0);
    }

    public void draw(PVector p) {
        this.draw(parent.g, floor(p.x), floor(p.y));
    }

    public void draw(float left, float top) {
        this.draw(parent.g, left, top);
    }

    public void draw(int n, PVector p) {
        this.draw(parent.g, n, p.x, p.y);
    }

    public void draw(int n, float left, float top) {
        this.draw(parent.g, n, left, top);
    }

    public void draw(PGraphics pg) {
        this.draw(pg, 0, 0);
    }

    public void draw(PGraphics pg, PVector p) {
        this.draw(pg, p.x, p.y);
    }

    public void draw(PGraphics pg, float left, float top) {
        this.prepareDraw(pg, left, top);
        for (int n = 0; n < layers.size(); n++) if (this.layers.get(n).visible) this.drawLayer(pg, n);
        this.finishDraw(pg);
    }

    public void draw(PGraphics pg, int n, PVector p) {
        this.draw(pg, n, p.x, p.y);
    }

    public void draw(PGraphics pg, int n, float left, float top) {
        if (n >= 0 && n < this.layers.size()) {
            this.prepareDraw(pg, left, top);
            this.drawLayer(pg, n);
            this.finishDraw(pg);
        }
    }

    //Layers methods

    public String getType(int n) {
        if (n >= 0 && n < this.layers.size()) return this.layers.get(n).type;
        else return null;
    }

    public boolean getVisible(int n) {
        if (n >= 0 && n < this.layers.size()) return this.layers.get(n).visible;
        else return false;
    }

    public void setVisible(int n, boolean v) {
        if (n >= 0 && n < this.layers.size()) this.layers.get(n).visible = v;
    }

    public PImage getImage(int n) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("imagelayer"))
            return this.layers.get(n).image;
        else return null;
    }

    public ArrayList<StringDict> getObjects(int n) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("objectgroup"))
            return this.layers.get(n).objects;
        else return null;
    }

    public int getObjectsColor(int n) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("objectgroup"))
            return this.layers.get(n).objcolor;
        else return 0;
    }

    public int[] getData(int n) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("layer")) return this.layers.get(n).data;
        else return null;
    }

    public Layer getLayer(int n) {
        if (n >= 0 && n < this.layers.size()) return this.layers.get(n);
        else return null;
    }

    public ArrayList<CollisionShape> getShapes(int n) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("layer"))
            return this.layers.get(n).getShapes();
        else return null;
    }

    public ArrayList<CollisionShape> getShapes(int n, int x, int y) {
        if (n >= 0 && n < this.layers.size() && this.layers.get(n).type.equals("layer"))
            return this.layers.get(n).getShapesAt(x, y);
        else return null;
    }

    public StringDict getCustomProperties(int n) {
        if (n >= 0 && n < this.layers.size()) return this.layers.get(n).properties;
        else return null;
    }

    public float getOpacity(int n) {
        if (n >= 0 && n < this.layers.size()) return this.layers.get(n).opacity;
        else return 0;
    }

    public void setOpacity(int n, float o) {
        if (n >= 0 && n < this.layers.size() && o >= 0 && o <= 1)
            this.layers.get(n).opacity = min(max(0, o), 1);
    }

    public int getTileIndex(int n, int x, int y) {
        if (n >= 0 && n < this.layers.size() && x >= 0 && y >= 0 && x < this.mapwidth && y < this.mapheight && this.layers.get(n).type.equals("layer"))
            return this.layers.get(n).data[x + y * this.mapwidth] - 1;
        else return -2;
    }

    public void setTileIndex(int n, int x, int y, int v) {
        if (n >= 0 && n < this.layers.size() && x >= 0 && y >= 0 && x < this.mapwidth && y < this.mapheight && this.layers.get(n).type.equals("layer") && v >= -1)
            this.layers.get(n).data[x + y * this.mapwidth] = v + 1;
    }

    public void toImage(int n) {
        if (n >= 0 && n < this.layers.size()) {
            Layer l = this.layers.get(n);
            int fw = this.camwidth;
            int fh = this.camheight;
            int ml = this.camleft;
            int mt = this.camtop;
            boolean v = l.visible;
            l.visible = true;
            this.camleft = 0;
            this.camtop = 0;
            switch (this.orientation) {
                case "orthogonal":
                    this.camwidth = this.mapwidth * this.tilewidth;
                    this.camheight = this.mapheight * this.tileheight;
                    break;
                case "isometric":
                    this.camwidth = (this.mapwidth + this.mapheight) * this.tilewidth / 2;
                    this.camheight = (this.mapwidth + this.mapheight) * this.tileheight / 2;
                    break;
                case "staggered":
                case "hexagonal":
                    if (this.staggeraxis.equals("x")) {
                        this.camwidth = (this.mapwidth + 1) * (this.hexsidelength + this.tilewidth) / 2;
                        this.camheight = (this.mapheight + 1) * (this.hexsidelength + this.tileheight);
                    } else {
                        this.camwidth = (this.mapwidth + 1) * (this.hexsidelength + this.tilewidth);
                        this.camheight = (this.mapheight + 1) * (this.hexsidelength + this.tileheight) / 2;
                    }
                    break;
            }
            PGraphics imageGraphics = parent.createGraphics(this.camwidth, this.camheight);
            imageGraphics.beginDraw();
            this.drawLayer(imageGraphics, n);
            imageGraphics.endDraw();
            l.image = imageGraphics;
            l.data = null;
            l.objects = null;
            l.type = "imagelayer";
            l.visible = v;
            l.offsetx = 0;
            l.offsety = 0;
            this.camwidth = fw;
            this.camheight = fh;
            this.camleft = ml;
            this.camtop = mt;
        }
    }

    //Map methods

    public String getFilename() {
        return this.filename;
    }

    public float getVersion() {
        return this.version;
    }

    public int getBackgroundColor() {
        return this.backgroundcolor;
    }

    public PVector getMapSize() {
        return new PVector(this.mapwidth, this.mapheight);
    }

    public PVector getTileSize() {
        return new PVector(this.tilewidth, this.tileheight);
    }

    public int getHexSideLength() {
        return this.hexsidelength;
    }

    public PVector getCamCorner() {
        if (this.positionmode.equals("MAP")) return this.canvasToMap(this.camleft, this.camtop);
        else return new PVector(this.camleft, this.camtop);
    }

    public PVector getCamCenter() {
        if (this.positionmode.equals("MAP"))
            return this.canvasToMap(this.camleft + this.camwidth / 2, this.camtop + this.camheight / 2);
        else return new PVector(this.camleft + this.camwidth / 2, this.camtop + this.camheight / 2);
    }

    public PVector getCamSize() {
        return new PVector(this.camwidth, this.camheight);
    }

    public void setCamSize(PVector s) { //Only useful for some pre-draw calculations, since size is always the last PGraphics used.
        this.setCamSize(floor(s.x), floor(s.y));
    }

    public void setCamSize(int w, int h) {
        this.camwidth = w;
        this.camheight = h;
    }

    public int getDrawMargin() {
        return this.drawmargin;
    }

    public void setDrawMargin(int n) { //for orthogonal maps
        this.drawmargin = max(1, n);
    }

    public int getDrawMode() {//CORNER or CENTER
        return this.drawmode;
    }

    public void setDrawMode(int s) {
        if (s == PConstants.CORNER || s == PConstants.CENTER) this.drawmode = s;
    }

    public String getPositionMode() {//"CANVAS" or "MAP"
        return this.positionmode;
    }

    public void setPositionMode(String s) {
        if (s.equals("CANVAS") || s.equals("MAP")) this.positionmode = s;
    }

    public PVector getPosition() {
        if (this.drawmode == parent.CORNER) return this.getCamCorner();
        else return this.getCamCenter();
    }

    //Coordinate methods

    public PVector canvasToMap(PVector p) {
        return this.canvasToMap(p.x, p.y);
    }

    public PVector canvasToMap(float x, float y) {
        return map.canvasToMap(x, y);
    }

    public PVector mapToCanvas(PVector p) {
        return this.mapToCanvas(p.x, p.y);
    }

    public PVector mapToCanvas(float nx, float ny) {
        return map.mapToCanvas(nx, ny);
    }

    public PVector camToCanvas(PVector p) {
        return this.camToCanvas(p.x, p.y);
    }

    public PVector camToCanvas(float x, float y) {
        return new PVector(x + this.camleft, y + this.camtop);
    }

    public PVector canvasToCam(PVector p) {
        return this.canvasToCam(p.x, p.y);
    }

    public PVector canvasToCam(float x, float y) {
        return new PVector(x - this.camleft, y - this.camtop);
    }

    public PVector camToMap(PVector p) {
        return this.camToMap(p.x, p.y);
    }

    public PVector camToMap(float x, float y) {
        return this.canvasToMap(this.camToCanvas(x, y));
    }

    public PVector mapToCam(PVector p) {
        return this.mapToCam(p.x, p.y);
    }

    public PVector mapToCam(float nx, float ny) {
        return this.canvasToCam(this.mapToCanvas(nx, ny));
    }
}