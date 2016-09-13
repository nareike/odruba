package odruba.pojo.vis;

import java.io.Serializable;

public class Font implements Serializable {

    private String color;
    private Integer size;
    private String background;

    public Font() {
        // nothing to do
    }

    public Font(String color) {
        this.color = color;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // --- G E T T E R S ---

    public String getColor() {
        return color;
    }

    public String getBackground() {
        return background;
    }

    public Integer getSize() {
        return size;
    }
}
