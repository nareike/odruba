package odruba;

import org.apache.jena.rdf.model.* ;

import java.util.ArrayList;

/**
 * VIS vocabulary items
 */
public class VIS {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri="http://vis.example.org/";
    public static final String NS ="http://vis.example.org/";

    protected static final Resource resource( String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource ColorCombination = resource("ColorCombination");

    public static final Resource Config = resource("Config");
    public static final Resource Hidden = resource("Hidden");

    public static final Property labelProperty = property("labelProperty");
    public static final Property titleProperties = property("titleProperties");
    public static final Property expandOutgoingProperties = property("expandOutgoingProperties");
    public static final Property expandIncomingProperties = property("expandIncomingProperties");
    public static final Property showLiterals = property("showLiterals");
    public static final Property showClasses = property("showClasses");
    public static final Property maxLabelLength = property("maxLabelLength");

    public static final Property allowExpansion = property("allowExpansion");
    public static final Property limitExpansionToProperties = property("limitExpansionToProperties");

    public static final Property dataSource = property("dataSource");
    public static final Property ontologySource = property("ontologySource");
    public static final Property ruleSets = property("ruleSets");

    public static final Property color = property("color");
    public static final Property highlight = property("highlight");
    public static final Property borderWidth = property("borderWidth");
    public static final Property background = property("background");
    public static final Property border = property("border");
    public static final Property shadow = property("shadow");
    public static final Property glow = property("glow");
    public static final Property size = property("size");
    public static final Property shape = property("shape");
    public static final Property group = property("group");
    public static final Property width = property("width");
    public static final Property label = property("label");
    public static final Property hidden = property("hidden");

    public static final Property icon = property("icon");

    public static final Property fontColor = property("fontColor");
    public static final Property fontSize = property("fontSize");

    public static final Property defaultShape = property("defaultShape");
    public static final Property showLabels = property("showLabels");

    public static final ArrayList<Property> datatypeProperties = new ArrayList<Property>() {{
        add(glow);
        add(size);
        add(shadow);
        add(shape);
        add(borderWidth);
        add(group);
        add(fontColor);
        add(fontSize);
        add(icon);
    }};

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
