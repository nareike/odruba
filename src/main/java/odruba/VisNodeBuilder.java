package odruba;

import odruba.fontawesome.FontAwesomeDictionary;
import odruba.pojo.vis.*;

public class VisNodeBuilder {

    private FontAwesomeDictionary fontAwesomeDictionary;

    private Integer id;
    private String label;
    private String shape;
    private String icon;
    private String backgroundColor;
    private String borderColor;
    private String title;
    private Integer size;

    private String highlight_backgroundColor;
    private String highlight_borderColor;

    private String glow_color;

    private String font_color;
    private Integer font_size;

    private String group;
    private Boolean dashedBorder = false;

    public VisNodeBuilder(FontAwesomeDictionary fontAwesomeDictionary) {
        this.fontAwesomeDictionary = fontAwesomeDictionary;
    }

    public VisNodeBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public VisNodeBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public VisNodeBuilder setColor(String backgroundColor, String borderColor) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        return this;
    }

    public VisNodeBuilder setHighlight(String backgroundColor, String borderColor) {
        this.highlight_backgroundColor = backgroundColor;
        this.highlight_borderColor = borderColor;
        return this;
    }

    public VisNodeBuilder setFontColor(String color) {
        this.font_color = color;
        return this;
    }

    public VisNodeBuilder setFontSize(Integer font_size) {
        this.font_size = font_size;
        return this;
    }

    public VisNodeBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public VisNodeBuilder setShape(String shape) {
        this.shape = shape;
        return this;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public VisNodeBuilder setSize(Integer size) {
        this.size = size;
        return this;
    }

    public VisNodeBuilder setGroup(String group) {
        this.group = group;
        return this;
    }

    public VisNodeBuilder setGlowColor(String color) {
        glow_color = color;
        return this;
    }

    public VisNodeBuilder setDashedBorder() {
        this.dashedBorder = true;
        return this;
    }

    // --- B U I L D   M E T H O D ---

    public Node build() {
        Node newNode = new Node();
        if (id != null) {
            newNode.setId(id);
        }

        if (label != null) {
            newNode.setLabel(label);
        }

        if (backgroundColor != null && borderColor != null) {
            newNode.setColor(colorNode());
        }

        if (font_color != null || font_size != null) {
            newNode.setFont(fontNode());
        }

        if (glow_color != null) {
            newNode.setShadow(glowNode());
        }

        if (group != null) {
            newNode.setGroup(group);
        }

        if (shape != null && icon == null) {
            newNode.setShape(shape);
        }

        if (icon != null) {
            newNode.setShape("icon");
            Icon ic = new Icon();
            ic.setCode(Character.toString((char) Integer.parseInt(fontAwesomeDictionary.getUnicodeByID(icon), 16)));
            ic.setFace("FontAwesome");
            if (borderColor != null) {
                ic.setColor(borderColor);
            }
            else {
                ic.setColor("black");
            }
            newNode.setIcon(ic);
        }

        if (size != null) {
            newNode.setSize(size);
        }

        if (title != null) {
            newNode.setTitle(title);
        }

        if (dashedBorder) {
            newNode.setShapeProperties(shapeProperties());
        }

        return newNode;
    }

    // --- P R I V A T E   M E T H O D S ---

    private Font fontNode() {
        Font font = new Font();
        if(font_color != null) {
            font.setColor(font_color);
        }
        if(font_size != null) {
            font.setSize(font_size);
        }
        return font;
    }

    private Shadow glowNode() {
        Shadow shadow = new Shadow(glow_color, 0, 0, 10);
        return shadow;
    }

    private Color colorNode() {
        Color color = new Color(backgroundColor, borderColor);

        if (highlight_backgroundColor != null && highlight_borderColor != null) {
            color.setHighlight(highlight_backgroundColor, highlight_borderColor);
        }

        return color;
    }

    private ShapeProperties shapeProperties() {
        ShapeProperties shapeProperties = new ShapeProperties();
        shapeProperties.setBorderDashes(5, 5);
        return shapeProperties;
    }

}
