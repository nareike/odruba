package odruba.pojo.vis;

public class Options {

    private Node nodes;
    private Edge edges;

    public Options() {
        nodes = new Node();
        Color color = new Color("#EEEEEE", "#666666");
        color.setHighlight("white", "black");
        nodes.setColor(color);

        edges = new Edge();
        EdgeColor edgeColor = new EdgeColor();
        edgeColor.setColor("black");
        edgeColor.setHighlight("black");
        edges.setColor(edgeColor);
    }

    public Options(Node nodes, Edge edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    // --- G E T T E R S ---

    public Node getNodes() {
        return nodes;
    }

    public Edge getEdges() {
        return edges;
    }
}
