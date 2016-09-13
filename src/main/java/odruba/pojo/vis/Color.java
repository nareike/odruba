package odruba.pojo.vis;

import java.io.Serializable;

public class Color implements Serializable {

    private String background;
    private String border;
    private Color highlight;

    public Color() {
    }

    public Color(String background, String border) {
        this.background = background;
        this.border = border;
    }

    public void setHighlight(String background, String border) {
        highlight = new Color(background, border);
    }

    // --- G E T T E R S ---

    public String getBackground() {
        return background;
    }

    public String getBorder() {
        return border;
    }

    public Color getHighlight() {
        return highlight;
    }

}
