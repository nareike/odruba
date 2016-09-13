package odruba;

import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import odruba.config.GraphConfiguration;
import odruba.config.ModelHerder;
import odruba.fontawesome.FontAwesomeDictionary;
import odruba.pojo.vis.*;
import odruba.service.ModelTools;
import odruba.service.VisNodeService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This service builds a graph object (pojo.vis.Graph) that can
 * directly be passed to the front end.
 */
@Controller
public class VisGraphBuilder {

    private VisRDFModel visRDFModel;

    @Autowired
    private FontAwesomeDictionary fontAwesomeDictionary;

    @Autowired
    private ModelTools modelTools;

    @Autowired
    private ModelHerder modelHerder;

    @Autowired
    private GraphConfiguration graphConfiguration;

    @Autowired
    private VisNodeService visNodeService;

    /**
     * nodes: will hold the nodes of the graph
     * edges: will hold the edges of the graph
     */
    private List<Node> nodes;
    private List<Edge> edges;

    private Set<Resource> nodes2construct;

    // TODO: Move to config
    private Set<String> displayedNamespaces = new HashSet<String>() {{
        add("http://example.org/");
        add("http://data.linkedmdb.org/resource/film/");
        add("http://data.linkedmdb.org/resource/actor/");
        add("http://data.linkedmdb.org/resource/film_genre/");
        add("http://vocab.cv-tec.de/");
        //add(VIS.NS);
        add(null);
    }};

    private Set<String> hiddenNamespaces = new HashSet<String>() {{
        add(VIS.NS);
    }};

    private Boolean isAdded(RDFNode rdfNode) {
        return nodes.stream().anyMatch((Node n) -> n.getId() == getId(rdfNode));
    }

    private String getLabel(RDFNode n) {
        if (graphConfiguration.showLabels()) {
            String labelString;
            Property labelProperty = graphConfiguration.getLabelProperty();
            //Model model = visRDFModel.getVisModel();
            Model model = modelHerder.getInfModel();
            RDFNode rdfNode = model.getRDFNode(n.asNode());

            if (rdfNode.isResource()) {
                Resource resource = rdfNode.asResource();
                if (resource.hasProperty(labelProperty)) {
                    RDFNode label = resource.getProperty(labelProperty).getObject();
                    labelString = label.asLiteral().getString();
                } else {
                    // don't display a random hash for blank nodes without label
                    if (resource.isAnon()) {
                        labelString = "";
                    } else {
                        labelString = model.shortForm(resource.getURI());
                        if (labelString.equals(resource.getURI())) {
                            String ns = modelTools.namespaceHack(labelString);
                            labelString = labelString.replace(ns, "");
                        }
                    }
                }
            } else {
                labelString = rdfNode.toString();
            }

            Integer maxLength = graphConfiguration.getMaxLabelLength();
            if (maxLength != null && labelString.length() > maxLength) {
                labelString = labelString.substring(0, graphConfiguration.getMaxLabelLength()) + "...";
            }

            return labelString;
        }
        else {
            return "";
        }
    }

    private int getId(RDFNode n) {
        return n.hashCode();
    }

    private void constructLiteral(RDFNode literalNode) {
        if(isAdded(literalNode)) {
            return;
        }

        int id = getId(literalNode);
        VisNodeBuilder visNodeBuilder = new VisNodeBuilder(fontAwesomeDictionary);

        visNodeBuilder
                .setId(id)
                .setGroup("literal")
                .setLabel(literalNode.asLiteral().toString())
                .setShape("box");

        Node vis4jNode = visNodeBuilder.build();
        // TODO: Do this with the builder
        ShapeProperties shapeProp = new ShapeProperties();
        shapeProp.setBorderRadius(0);
        vis4jNode.setShapeProperties(shapeProp);
        nodes.add(vis4jNode);
    }

    private String getChainedPropertyValue(Resource r, Property... props) {
        return modelTools.getChainedPropertyValue(modelHerder.getInfModel(), r, props);
    }

    private String getValue(Resource r, Property p) {
        return modelTools.getValue(modelHerder.getInfModel(), r, p);
    }

    private List<String> getValues(Resource r, Property p) {
        return modelTools.getValues(modelHerder.getInfModel(), r, p);
    }

    private Boolean hasValue(Resource r, Property p) {
        return modelTools.hasValue(modelHerder.getInfModel(), r, p);
    }

    public Set<Resource> rdfTypes(Resource r) {
        return modelTools.rdfTypes(modelHerder.getInfModel(), r);
    }

    private Boolean showNode(RDFNode n) {
        if (!n.isResource()) {
            return true;
        }

        Boolean display = true;
        String this_ns = modelTools.namespaceHack(n.asResource().getURI());

        if (this_ns != null) {
            for (String a_ns : hiddenNamespaces) {
                if (this_ns.startsWith(a_ns)) {
                    display = false;
                }
            }
        }
        return display;
    }

    private void constructNode(Resource subject) {
        String uuri = new String();
        if (subject.isResource()) {
            if (subject.isAnon()) {
                uuri = "_:" + subject.asNode().getBlankNodeId().toString();
            }
            else {
                uuri = subject.getURI();
            }
        }
        nodes.add(visNodeService.constructNode(subject, uuri));
    }

    private void constructEdge(int from, int to, String label, DashType dashType, String uri) {
        Edge edge = new Edge(from, to, label, dashType);
        /*
        if (dashType != null) {
            edge.setWidth(1);
        }
        */
        edge.setURI(uri);
        edge.setTitle("<div>" + uri + "</div");
        edges.add(edge);
    }

    private void parseStatement(Statement statement, DashType dashType) {
        switch (modelHerder.getProvenance(statement)) {
            case ONTOLOGY:
                dashType = DashType.DASHED;
                break;
            case REASONER:
                dashType = DashType.DOTTED;
                break;
        }

        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode  object = statement.getObject();

        if (showNode(subject)) {
            nodes2construct.add(subject);
            //constructNode(subject);
        }

        if (showNode(object)) {
            if (object.isResource()) {
                nodes2construct.add(object.asResource());
                //constructNode(object.asResource());
                // TODO: if(displayedNamespaces.contains(predicate.getNameSpace())) {
                if (showNode(subject)) {
                    constructEdge(
                            getId(subject),
                            getId(object),
                            getLabel(predicate),
                            dashType,
                            predicate.getURI()
                    );
                }
            } else if (object.isLiteral()) {
                constructLiteral(object);
                if (showNode(subject)) {
                    constructEdge(
                            getId(subject),
                            getId(object),
                            getLabel(predicate),
                            dashType,
                            predicate.getURI()
                    );
                }
            }
        }

    }

    public Graph model2json(VisRDFModel visRDFModel) {
        //System.out.println("> model2json");
        long startTime = System.nanoTime();

        this.visRDFModel = visRDFModel;
        Model model = visRDFModel.getVisModel();

        // list of nodes and edges in the visual graph
        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        StmtIterator iter = model.listStatements();

        nodes2construct = new HashSet<>();

        while(iter.hasNext()) {
            Statement statement = iter.next();
            parseStatement(statement, null);
        }

        nodes2construct.forEach(this::constructNode);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("> Duration: " + duration / 1000000 + " ms");
        return build();
    }

    private Options buildOptions() {
        Node nodes = new Node();
        Color color = new Color("#EEEEEE", "#666666");
        //color.setHighlight("white", "black");

        if (graphConfiguration.getDefaultShape() != null) {
            nodes.setShape(graphConfiguration.getDefaultShape());
        }

        nodes.setColor(color);

        Edge edges = new Edge();
        //EdgeColor edgeColor = new EdgeColor();
        //edgeColor.setColor("black");
        //edgeColor.setHighlight("black");
        //edges.setColor(edgeColor);
        //edges.setLength(500);

        return new Options(nodes, edges);
    }

    public Graph build() {
        Graph graph = new Graph();
        graph.setNodes(nodes);
        graph.setEdges(edges);
        graph.setOptions(buildOptions());
        return graph;
    }

}
