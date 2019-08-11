package byog.Core;

public class Portal {
    Portal pair;
    Point location;

    public Portal(Point p) {
        location = p;
    }

    public Portal(Point p, Portal portal2) {
        location = p;
        pair = portal2;
        pair.pair = this;
    }

    public Portal getPair() {
        return pair;
    }

    public Point getLocation() {
        return location;
    }
}
