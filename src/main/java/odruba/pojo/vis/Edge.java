package odruba.pojo.vis;

import odruba.DashType;

import java.util.List;

public class Edge {

    private Integer from;
    private Integer to;
    private Integer width;
    private String arrows;
    private String label;
    private List<Integer> dashes;
    private EdgeColor color;
    private String title;
    private Integer length;

    // an URI is not part of the vis.js specification, it is included
    // for this project solely
    private String uri;

    public Edge() {
    }

    public Edge(int from, int to, String arrows, String label) {
        this.from = from;
        this.to = to;
        this.arrows = arrows;
        this.label = label;
    }

    public Edge(int from, int to, String label, DashType dashes) {
        this.from = from;
        this.to = to;
        this.label = label;
        this.arrows = "to";
        if(dashes != null) {
            this.dashes = dashes.asList();
        }
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setDashed() {
        dashes = DashType.DASHED.asList();
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public void setColor(EdgeColor color) {
        this.color = color;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // --- G E T T E R S ---


    public Integer getLength() {
        return length;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    public Integer getWidth() {
        return width;
    }

    public String getArrows() {
        return arrows;
    }

    public String getLabel() {
        return label;
    }

    public List<Integer> getDashes() {
        return dashes;
    }

    public String getURI() {
        return uri;
    }

    public EdgeColor getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

}
