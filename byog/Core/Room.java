package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Room implements Serializable {
    private static Random rand;
    private Point bL;
    private Point tR;
    private TETile[][] parMap;
    static boolean key = false;
    static boolean hasDoor = false;
    private static final long serialVersionUID = 12398234798734234L;

    public Room(int bX, int bY, int tX, int tY, TETile[][] map) {
        bL = new Point(bX, bY);
        tR = new Point(tX, tY);
        parMap = map;
    }

    public Room(Point bottomLeft, Point topRight, TETile[][] map) {
        bL = bottomLeft;
        tR = topRight;
        parMap = map;
    }

    public static void setRand(Random r) {
        rand = r;
    }

    public void drawRoom() {
        for (int i = bL.getX(); i < tR.getX(); i++) {
            for (int j = bL.getY(); j < tR.getY(); j++) {
                if (rand.nextInt(100) > 97) {
                    parMap[i][j] = Tileset.FLOWER;
                } else {
                    parMap[i][j] = Tileset.FLOOR;
                }
            }
        }
    }


    public Point getPoint() {
        int newX = rand.nextInt(Math.abs(tR.getX() - bL.getX() + 1)) + bL.getX();
        int newY = rand.nextInt(Math.abs(tR.getY() - bL.getY() + 1)) + bL.getY();
        return new Point(newX, newY);
    }

    static void makePath(Room r1, Room r2) {
        try {
            if (r1.parMap != r2.parMap) {
                throw new RuntimeException();
            }
            int c1x = ((r1.bL.getX() + r1.tR.getX()) / 2);
            int c1y = ((r1.bL.getY() + r1.tR.getY()) / 2);
            int c2x = ((r2.bL.getX() + r2.tR.getX()) / 2);
            int c2y = ((r2.bL.getY() + r2.tR.getY()) / 2);
            Point center1 = new Point(c1x, c1y);
            Point center2 = new Point(c2x, c2y);
            int currX = center1.getX();
            int currY = center1.getY();
            while (!((currX == center2.getX()) && (currY == center2.getY()))) {
                if (rand.nextInt(100) > 97) {
                    r1.parMap[currX][currY] = Tileset.FLOWER;
                } else {
                    r1.parMap[currX][currY] = Tileset.FLOOR;
                }
                if (Math.abs(currX - center2.getX()) != 0) {
                    if (Math.abs(currX - center2.getX()) <= 1) {
                        currX = center2.getX();
                    } else if (center2.getX() - currX > 0) {
                        currX++;
                    } else {
                        currX--;
                    }
                } else if (Math.abs(currY - center2.getY()) != 0) {
                    if (Math.abs(currY - center2.getY()) <= 1) {
                        currY = center2.getY();
                    } else if (center2.getY() - currY > 0) {
                        currY++;
                    } else {
                        currY--;
                    }
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Connecting room from diff levels " + e.getMessage());
        }
    }

    static void keydoor(TETile[][] map, World world) {
        int k = rand.nextInt(World.WIDTH);
        int m = rand.nextInt(World.HEIGHT);
        Point l = new Point(k, m);
        while (!world.isFloor(l)) {
            l.setX(rand.nextInt(World.WIDTH));
            l.setY(rand.nextInt(World.HEIGHT));
        }
        map[l.getX()][l.getY()] = Tileset.TREE;
        int i = rand.nextInt(World.WIDTH - 1);
        int j = rand.nextInt(World.HEIGHT - 1);
        Point p = new Point(i, j);
        while (!world.isCorner(p)) {
            p.setX(rand.nextInt(World.WIDTH - 1));
            p.setY(rand.nextInt(World.HEIGHT - 1));
        }
        map[p.getX()][p.getY()] = Tileset.LOCKED_DOOR;
        p.setY(p.getY() + 1);
        if (world.isWall(p)) {
            map[p.getX()][p.getY()] = Tileset.LOCKED_DOOR;
            return;
        }
        p.setY(p.getY() - 2);
        if (world.isWall(p)) {
            map[p.getX()][p.getY()] = Tileset.LOCKED_DOOR;
            return;
        }
        p.setY(p.getY() + 1);
        p.setX(p.getX() + 1);
        if (world.isWall(p)) {
            map[p.getX()][p.getY()] = Tileset.LOCKED_DOOR;
            return;
        }
        p.setX(p.getX() - 2);
        if (world.isWall(p)) {
            map[p.getX()][p.getY()] = Tileset.LOCKED_DOOR;
            return;
        }
    }
}
