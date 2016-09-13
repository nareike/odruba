package odruba.pojo.vis;

public class EdgeColor {

    private String color;
    private String highlight;
    private String hover;
    private Float opacity;
    // TODO inherit

    public void setColor(String color) {
        this.color = color;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public void setHover(String hover) {
        this.hover = hover;
    }

    public void setOpacity(Float opacity) {
        this.opacity = opacity;
    }

    // --- G E T T E R S ---


    public String getColor() {
        return color;
    }

    public String getHighlight() {
        return highlight;
    }

    public String getHover() {
        return hover;
    }

    public Float getOpacity() {
        return opacity;
    }

}
