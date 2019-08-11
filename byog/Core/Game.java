package byog.Core;

import byog.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Game {
    private static World gameWorld;
    private String inputSeq;
    private boolean quitSeq;
    private boolean gameStarted;
    private boolean settingSeed;
    private static long seed;
    private static final long serialVersionUID = 12345634798734234L;

    public Game() {
        inputSeq = "";
        quitSeq = false;
        gameStarted = false;
        settingSeed = false;
        seed = 0;
    }

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        StdDraw.enableDoubleBuffering();
        gameWorld = new World();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                parse(StdDraw.nextKeyTyped());
                gameWorld.hungercounter();
            }
            if (gameStarted) {
                gameWorld.updatemousepointer();
                gameWorld.hudmenu();
            }
            if (gameWorld.dead) {
                System.exit(0);
            }
            gameWorld.renderWorld();
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a  series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        for (char command : input.toCharArray()) {
            if (parse(command)) {
                break;
            }
            gameWorld.hungercounter();
        }
        return gameWorld.map;
    }

    /**
     * @param command the char input
     * @return true if quitting so caller can return world state
     */
    public boolean parse(char command) {
        switch (command) {
            case 'n':
            case 'N':
                inputSeq = inputSeq + command;
                if (!gameStarted) {
                    settingSeed = true;
                }
                return false;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '0':
                if (settingSeed) {
                    inputSeq = inputSeq + command;
                    seed = 10 * seed + Character.getNumericValue(command);
                }
                return false;
            case ':':
                quitSeq = true;
                return false;
            case 'q':
            case 'Q':
                if (quitSeq) {
                    save();
                    System.exit(0);
                }
                return quitSeq;
            case 'l':
            case 'L':
                if (!gameStarted) {
                    load();
                }
                gameStarted = true;
                return false;
            /*
            The following commands are for the world to handle
             */
            case 'w':
            case 'W':
            case 'a':
            case 'A':
            case 's':
            case 'S':
            case 'd':
            case 'D':
                inputSeq = inputSeq + command;
                if (!gameStarted && command == 's') {
                    gameStarted = true;
                    gameWorld = new World(seed);
                } else if (!gameStarted) {
                    return false;
                } else {
                    gameWorld.handle(command);
                }
                //Make game call move

                return false;
            default:
                return false;
        }
    }

    public static void nextlevel() {
        seed = (seed + 1) * 2;
        int count = gameWorld.stepcount;
        int damage = gameWorld.damage;
        int health = gameWorld.p1health;
        int level = gameWorld.level;
        gameWorld = new World(seed);
        gameWorld.level = level + 1;
        gameWorld.stepcount = count;
        gameWorld.damage = damage;
        gameWorld.p1health = health;
    }

    public void save() {
        File f = new File("./world.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            System.out.println(inputSeq);
            os.writeObject(inputSeq);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public void load() {
        File f = new File("./world.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                String load = (String) os.readObject();
                os.close();
                playWithInputString(load);
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        /* In the case no World has been saved yet, we return a new one. */
        settingSeed = true;
    }
}
