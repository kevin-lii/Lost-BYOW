package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;

import static byog.TileEngine.Tileset.*;

public class Player implements Serializable {
    private TETile avatar = Tileset.PLAYER;
    private World pWorld;
    private static final long serialVersionUID = 12348234798734234L;
    Point location;
    private TETile[][] parMap;

    public Player(Point p, World world) {
        location = p;
        pWorld = world;
        parMap = pWorld.map;
        parMap[p.getX()][p.getY()] = avatar;
    }

    private void moveTo(int x, int y) {
        Point p = new Point(x, y);
        if (pWorld.isKey(p) || pWorld.isFood(p) || pWorld.isFloor(p) || pWorld.isPortal(p)) {
            if (pWorld.isKey(p)) {
                pWorld.haskey = true;
            }
            if (pWorld.isFood(p)) {
                pWorld.food = true;
            }
            if (pWorld.isPortal(p)) {
                Point dest = pWorld.getPortal(p).pair.location;
                parMap[location.getX()][location.getY()] = FLOOR;
                location.setX(dest.getX());
                location.setY(dest.getY());
                parMap[location.getX()][location.getY()] = PLAYER;
            } else {
                if (pWorld.isPortal(location)) {
                    parMap[location.getX()][location.getY()] = UNLOCKED_DOOR;
                } else {
                    parMap[location.getX()][location.getY()] = FLOOR;
                }
                location.setX(x);
                location.setY(y);
                parMap[location.getX()][location.getY()] = PLAYER;
            }
        }
        if (pWorld.isLockedDoor(p) && pWorld.haskey) {
            location.setX(x);
            location.setY(y);
            parMap[location.getX()][location.getY()] = UNLOCKED_DOOR;
            World.doorunlocked = true;
            Game.nextlevel();
        }
    }

    public void move(char input) {
        switch (input) {
            case 'd':
            case 'D':
                moveTo(location.getX() + 1, location.getY());
                break;
            case 's':
            case 'S':
                moveTo(location.getX(), location.getY() - 1);
                break;
            case 'a':
            case 'A':
                moveTo(location.getX() - 1, location.getY());
                break;
            case 'w':
            case 'W':
                moveTo(location.getX(), location.getY() + 1);
                break;
            default:
                break;
        }
    }
}
