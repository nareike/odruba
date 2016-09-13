package odruba.pojo.vis;

import java.io.Serializable;

public class Shadow implements Serializable {

    private String color;
    private int x;
    private int y;
    private int size;

    public Shadow(String color, int x, int y, int size) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // --- G E T T E R S ---

    public String getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}
