package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

import static byog.Core.Room.makePath;
import static byog.TileEngine.Tileset.*;

public class World implements Serializable {
    //Main World utils
    TETile[][] map;
    private TERenderer ter = new TERenderer();
    private Random rand;
    private Player player;
    private static final long serialVersionUID = 45498234798734234L;

    //More detailed information on the map itself
    static final int WIDTH = 40;
    static final int HEIGHT = 40;
    private final int MAXROOMS = 10;
    private Room[] rooms = new Room[MAXROOMS];
    private Portal[][] portalMap;

    //Information on HUD
    private static final String HEALTH = "  Hunger  ";
    int damage = 0;
    int p1health = HEALTH.length() - damage;
    int stepcount = 0;
    private static double mousex;
    private static double mousey;
    boolean dead = false;
    boolean food = false;
    boolean haskey = false;
    boolean haslockeddoor = false;
    static boolean doorunlocked;
    private long seed;
    int level = 0;

    //To initialize start screen
    public World() {
        ter.initialize(WIDTH, HEIGHT);
        map = startScreen();
    }

    public World(long seed) {
        this.seed = seed;
        doorunlocked = false;
        ter.initialize(WIDTH, HEIGHT);
        startGame();
    }

    public void startGame() {
        map = generateMap();
        Point p = rooms[rand.nextInt(MAXROOMS)].getPoint();
        while (!isFloor(p)) {
            p = rooms[rand.nextInt(MAXROOMS)].getPoint();
        }
        player = new Player(p, this);
    }

    public void renderWorld() {
        ter.renderFrame(map);
    }

    private TETile[][] startScreen() {
        TETile[][] title = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                title[x][y] = Tileset.NOTHING;
            }
        }
        char[] name = "Lost: The Game".toCharArray();
        char[] newgame = "New Game(N) and Enter Seed(#)".toCharArray();
        char[] confirmseed = "Confirm Seed(S)".toCharArray();
        char[] loadgame = "Load Game(L)".toCharArray();
        char[] quit = "Quit(Q)".toCharArray();
        for (int i = 0; i < name.length; i++) {
            title[(WIDTH - name.length) / 2 + i][HEIGHT * 3 / 4] =
                    new TETile(name[i], Color.white, Color.black, "title");
        }
        for (int i = 0; i < newgame.length; i++) {
            title[(WIDTH - newgame.length) / 2 + i][HEIGHT / 4 + 1] =
                    new TETile(newgame[i], Color.white, Color.black, "new");
        }
        for (int i = 0; i < confirmseed.length; i++) {
            title[(WIDTH - confirmseed.length) / 2 + i][HEIGHT / 4] =
                    new TETile(confirmseed[i], Color.white, Color.black, "confirm");
        }
        for (int i = 0; i < loadgame.length; i++) {
            title[(WIDTH - loadgame.length) / 2 + i][HEIGHT / 4 - 1] =
                    new TETile(loadgame[i], Color.white, Color.black, "load");
        }
        for (int i = 0; i < quit.length; i++) {
            title[(WIDTH - quit.length) / 2 + i][HEIGHT / 4 - 2] =
                    new TETile(quit[i], Color.white, Color.black, "quit");
        }
        return title;
    }

    private TETile[][] generateMap() {
        rand = new Random(seed);
        Room.setRand(rand);
        map = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                map[i][j] = Tileset.WALL;
            }
        }
        for (int i = 0; i < MAXROOMS; i++) {
            Point bL = getRandomPoint();
            int w = rand.nextInt(4) + 4;
            int h = rand.nextInt(4) + 4;
            int newX = Math.min(bL.getX() + w - 1, WIDTH - 1);
            int newY = Math.min(bL.getY() + h - 1, HEIGHT - 1);
            Point tR = new Point(newX, newY);
            rooms[i] = new Room(bL, tR, map);
            rooms[i].drawRoom();
        }

        for (int i = 0; i < MAXROOMS; i++) {
            makePath(rooms[i], rooms[(i + 1) % MAXROOMS]);
        }

        portalMap = new Portal[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                portalMap[i][j] = null;
            }
        }
        int i = 0;
        while (i < 2) {
            Point a = rooms[rand.nextInt(MAXROOMS)].getPoint();
            Point b = rooms[rand.nextInt(MAXROOMS)].getPoint();
            Portal port1 = new Portal(a);
            Portal port2 = new Portal(b, port1);

            if (portalMap[a.getX()][a.getY()] == null && portalMap[b.getX()][b.getY()] == null) {
                portalMap[a.getX()][a.getY()] = port1;
                portalMap[b.getX()][b.getY()] = port2;
                map[a.getX()][a.getY()] = UNLOCKED_DOOR;
                map[b.getX()][b.getY()] = UNLOCKED_DOOR;
                i++;
            }
        }
        cleanMap();
        Room.keydoor(map, this);
        return map;
    }

    public void handle(char command) {
        switch (command) {
            case 'w':
            case 'W':
            case 'a':
            case 'A':
            case 's':
            case 'S':
            case 'd':
            case 'D':
                player.move(command);
                stepcount += 1;
                break;
            default:
                break;
        }
    }

    //World Generation Utilities
    /*
    Ensures that walls are only used in borders, all unseen spaces are instead empty
     */
    private void cleanMap() {
        Point[] neighbors = new Point[8];
        boolean isWall = false;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                neighbors[0] = new Point(i - 1, j - 1);
                neighbors[1] = new Point(i, j - 1);
                neighbors[2] = new Point(i + 1, j - 1);
                neighbors[3] = new Point(i + 1, j);
                neighbors[4] = new Point(i + 1, j + 1);
                neighbors[5] = new Point(i, j + 1);
                neighbors[6] = new Point(i - 1, j + 1);
                neighbors[7] = new Point(i - 1, j);
                for (Point p : neighbors) {
                    if (inBounds(p) && (isFloor(p) || isKey(p) || isFood(p))) {
                        isWall = true;
                    }
                }
                if (!isWall) {
                    map[i][j] = Tileset.NOTHING;
                }
                isWall = false;
            }
        }
    }

    void hudmenu() {
        char[] mouse = updatemousepointer().description().toCharArray();
        char[] food2 = HEALTH.substring(0, p1health).toCharArray();
        char[] hunger = HEALTH.substring(p1health, HEALTH.length()).toCharArray();
        String num = "Level " + level;
        char[] levelnum = num.toCharArray();

        for (int i = 0; i < mouse.length; i++) {
            map[0 + i][HEIGHT - 1] =
                    new TETile(mouse[i], Color.white, Color.black, "mouse");
        }
        for (int i = 0; i < food2.length; i++) {
            map[WIDTH - HEALTH.length() + i][HEIGHT - 1] =
                    new TETile(food2[i], Color.black, Color.green, "food2");
        }
        for (int i = 0; i < hunger.length; i++) {
            map[WIDTH - hunger.length + i][HEIGHT - 1] =
                    new TETile(hunger[i], Color.black, Color.red, "hunger");
        }
        for (int i = mouse.length; i < WIDTH - HEALTH.length(); i++) {
            map[i][HEIGHT - 1] = Tileset.NOTHING;
        }
        for (int i = 0; i < num.length(); i++) {
            map[i + (WIDTH - num.length()) / 2][HEIGHT - 1] =
                    new TETile(levelnum[i], Color.white, Color.black, "level number");
        }
    }

    TETile updatemousepointer() {
        mousex = StdDraw.mouseX();
        mousey = StdDraw.mouseY();
        return map[Math.min((int) mousex, WIDTH - 1)][Math.min((int) mousey, HEIGHT - 1)];
    }

    void hungercounter() {
        if (stepcount > 15) {
            damage += 1;
            p1health -= 1;
            stepcount = 0;
        }
        if (stepcount < -15 && p1health < 10) {
            damage -= 1;
            p1health += 1;
        }
        if (stepcount < -15) {
            stepcount = 0;
        }
        if (food) {
            stepcount -= 15;
            food = false;
        }
        if (p1health <= 0) {
            dead = true;
        }
    }

    //General Utilities
    public boolean inBounds(Point p) {
        return (p.getX() >= 0 && p.getY() >= 0 && p.getX() < WIDTH && p.getY() < HEIGHT - 1);
    }

    public boolean isMapEdge(Point p) {
        return (p.getX() == 0 || p.getY() == 0 || p.getX() == WIDTH - 1 || p.getY() == HEIGHT - 2);
    }

    public boolean isFloor(Point p) {
        if (inBounds(p)) {
            return map[p.getX()][p.getY()].equals(FLOOR);
        }
        return false;
    }

    public boolean isWall(Point p) {
        if (inBounds(p)) {
            return map[p.getX()][p.getY()].equals(WALL);
        }
        return false;
    }

    public boolean isFood(Point p) {
        if (inBounds(p)) {
            return map[p.getX()][p.getY()].equals(FLOWER);
        }
        return false;
    }

    public boolean isKey(Point p) {
        if (inBounds(p)) {
            return map[p.getX()][p.getY()].equals(TREE);
        }
        return false;
    }

    public boolean isLockedDoor(Point p) {
        if (inBounds(p)) {
            return map[p.getX()][p.getY()].equals(LOCKED_DOOR);
        }
        return false;
    }

    public boolean isPortal(Point p) {
        if (inBounds(p)) {
            return portalMap[p.getX()][p.getY()] != null;
        }
        return false;
    }

    public Portal getPortal(Point p) {
        return portalMap[p.getX()][p.getY()];
    }


    public boolean isCorner(Point p) {
        int i = p.getX();
        int j = p.getY();
        Point a1 = new Point(i + 1, j);
        Point a2 = new Point(i - 1, j);
        Point b1 = new Point(i, j + 1);
        Point b2 = new Point(i, j - 1);
        if (isWall(a1) && isWall(b1)) {
            return isWall(p);
        }
        if (isWall(a2) && isWall(b2)) {
            return isWall(p);
        }
        if (isWall(a1) && isWall(b2)) {
            return isWall(p);
        }
        if (isWall(a2) && isWall(b1)) {
            return isWall(p);
        }
        if (isWall(p)) {
            return true;
        }
        return false;
    }

    public Point getRandomPoint() {
        return new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT - 1));
    }
}
