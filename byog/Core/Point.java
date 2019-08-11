package byog.Core;

import java.io.Serializable;

public class Point implements Serializable {
    private int x;
    private int y;
    private static final long serialVersionUID = 15498234798734234L;

    public Point(int xx, int yy) {
        x = xx;
        y = yy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int xx) {
        x = xx;
    }

    public void setY(int yy) {
        y = yy;
    }

    @Override
    public String toString() {
        return Integer.toString(x) + " " + Integer.toString(y);
    }
}
