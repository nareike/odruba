package odruba.pojo.vis;

import java.io.Serializable;

public class Node implements Serializable {

    private Integer id;
    private String label;
    // TODO: Class/enumeration for shapes
    private String shape;
    private String title;
    private String group;
    private Integer size;

    // an URI is not part of the vis.js specification, it is included
    // for this project solely
    private String uri;

    private Color color;
    private Font font;
    private Shadow shadow;
    private ShapeProperties shapeProperties;
    private Icon icon;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setShadow(Shadow shadow) {
        this.shadow = shadow;
    }

    public void setShapeProperties(ShapeProperties shapeProperties) {
        this.shapeProperties = shapeProperties;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    // --- G E T T E R S ---

    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getShape() {
        return shape;
    }

    public String getTitle() {
        return title;
    }

    public Color getColor() {
        return color;
    }

    public Integer getSize() {
        return size;
    }

    public Font getFont() {
        return font;
    }

    public Shadow getShadow() {
        return shadow;
    }

    public ShapeProperties getShapeProperties() {
        return shapeProperties;
    }

    public String getURI() {
        return uri;
    }

    public String getGroup() {
        return group;
    }

    public Icon getIcon() {
        return icon;
    }
}
