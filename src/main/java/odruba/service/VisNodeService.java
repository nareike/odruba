package odruba.service;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import odruba.Provenance;
import odruba.VIS;
import odruba.VisNodeBuilder;
import odruba.config.GraphConfiguration;
import odruba.config.ModelHerder;
import odruba.fontawesome.FontAwesomeDictionary;
import odruba.pojo.vis.Node;

import java.util.List;
import java.util.Set;

@Service
public class VisNodeService {

    @Autowired
    private FontAwesomeDictionary fontAwesomeDictionary;

    @Autowired
    private ModelTools modelTools;

    @Autowired
    private ModelHerder modelHerder;

    @Autowired
    private GraphConfiguration graphConfiguration;

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

    @Cacheable(value="nodes", key="#uuri")
    public Node constructNode(Resource subject, String uuri) {
        VisNodeBuilder visNodeBuilder = new VisNodeBuilder(fontAwesomeDictionary);
        int id = getId(subject);

        visNodeBuilder
                .setId(id)
                .setLabel(getLabel(subject));

        if (hasValue(subject, VIS.color)) {
            visNodeBuilder.setColor(
                    getChainedPropertyValue(subject, VIS.color, VIS.background),
                    getChainedPropertyValue(subject, VIS.color, VIS.border)
            );
        }

        if (hasValue(subject, VIS.highlight)) {
            visNodeBuilder.setHighlight(
                    getChainedPropertyValue(subject, VIS.highlight, VIS.background),
                    getChainedPropertyValue(subject, VIS.highlight, VIS.border)
            );
        }

        if (hasValue(subject, VIS.fontColor)) {
            visNodeBuilder.setFontColor(getValue(subject, VIS.fontColor));
        }

        if (hasValue(subject, VIS.fontSize)) {
            visNodeBuilder.setFontSize(Integer.parseInt(getValue(subject, VIS.fontSize)));
        }

        if (hasValue(subject, VIS.glow)) {
            visNodeBuilder.setGlowColor(getValue(subject, VIS.glow));
        }

        if (hasValue(subject, VIS.size)) {
            visNodeBuilder.setSize(Integer.parseInt(getValue(subject, VIS.size)));
        }

        if (hasValue(subject, VIS.shape)) {
            visNodeBuilder.setShape(getValue(subject, VIS.shape));
        }

        if (hasValue(subject, VIS.icon)) {
            visNodeBuilder.setIcon(getValue(subject, VIS.icon));
        }

        if(modelHerder.getProvenance(subject) != Provenance.GIVEN) {
            visNodeBuilder.setDashedBorder();
        }

        StringBuilder _title = new StringBuilder();
        for(Property p : graphConfiguration.getTitleProperties()) {
            if (hasValue(subject, p)) {
                // TODO: provide title builder
                _title.append("<div>");
                List<String> vals = getValues(subject, p);
                if (vals.size() == 1) {
                    _title.append(WordUtils.wrap(vals.get(0), 100).replace("\n", "</br>"));
                }
                else if (vals.size() > 1) {
                    _title.append("<ul>");
                    for(String val : vals) {
                        _title
                                .append("<li>")
                                .append(WordUtils.wrap(val, 100).replace("\n", "</br>"))
                                .append("</li>");
                    }
                    _title.append("</ul>");
                }
                _title.append("</div>\n");
            }
        }
        // TODO: make configurable
        if (subject.isResource()) {
            if (_title.length() == 0) {
                _title
                        .insert(0, "</div>")
                        .insert(0, subject.getURI())
                        .insert(0, "<div>");
            } else {
                _title
                        .insert(0, "</div>")
                        .insert(0, subject.getURI())
                        .insert(0, "<div style=\"padding-bottom: 5px; margin-bottom: 5px; border-bottom: 1px solid lightgray;\">");
            }
        }
        String title = _title.toString();
        if (!title.equals("")) {
            visNodeBuilder.setTitle(title);
        }

        Node vis4jNode = visNodeBuilder.build();
        vis4jNode.setURI(uuri);

        return vis4jNode;
    }
}
