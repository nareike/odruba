package odruba.config;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import odruba.VIS;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This component will manage various configuration properties by
 * providing convenience functions.
 */
@Service
public class GraphConfiguration {

    @Autowired
    private SetupDataContainer setupDataContainer;

    private String graphConfigFile;
    private String inputDataFile;
    private String ontologyDataFile;
    private List<String> rulesetFiles;

    private Model config = ModelFactory.createDefaultModel();
    private Resource configBase;

    private List<Property> titleProperties;
    private List<Property> expandOnProperties;
    private Property labelProperty = RDFS.label;
    private String dataSource;

    @Autowired
    public GraphConfiguration(
            @Value("${odruba.graph:src/main/resources/config/graph_config.ttl}") String graphConfigFile,
            @Value("${odruba.data.input:src/main/resources/demodata.ttl}") String inputDataFile,
            @Value("${odruba.data.ontology:src/main/resources/config/ontology_config.ttl}") String ontologyDataFile,
            RulesetList rulesetFiles,
            SetupDataContainer setupDataContainer
    ) throws IOException {
        this.setupDataContainer = setupDataContainer;
        this.graphConfigFile = graphConfigFile;
        this.inputDataFile = inputDataFile;
        this.ontologyDataFile = ontologyDataFile;
        this.rulesetFiles = rulesetFiles.getRulesets();

        loadInitialConfig();
        initialize(setupDataContainer);
    }

    /**
     * This method will load data from file the provided by the
     * application.yaml properties (or a different yaml file,
     * depending on the active Spring Boot profile). The loaded
     * text data will be converted to a string and put verbatim
     * into the setupDataContainer
     *
     * @throws IOException
     */
    public void loadInitialConfig() throws IOException {
        setupDataContainer.setGraphConfig(
                new String(Files.readAllBytes(Paths.get(graphConfigFile)),
                        StandardCharsets.UTF_8));
        setupDataContainer.setInputData(
                new String(Files.readAllBytes(Paths.get(inputDataFile)),
                        StandardCharsets.UTF_8));
        setupDataContainer.setOntologyData(
                new String(Files.readAllBytes(Paths.get(ontologyDataFile)),
                        StandardCharsets.UTF_8));

        List<RulesetData> rulesetDataList = new ArrayList<>();
        for(String filename : rulesetFiles) {
            RulesetData rulesetData = new RulesetData();
            Path path = Paths.get(filename);
            rulesetData.setName(path.getFileName().toString().replaceFirst("[.][^.]+$", ""));
            rulesetData.setData(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            rulesetDataList.add(rulesetData);
        }
        setupDataContainer.setRulesetData(rulesetDataList);
    }

    public Boolean setGraphConfigFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            graphConfigFile = filepath;
            initialize();
            return true;
        }
        return false;
    }

    public String getGraphConfigFile() {
        return graphConfigFile;
    }

    /**
     * Calls initialize() with the setupDataContainer field.
     */
    public void initialize() {
        initialize(setupDataContainer);
    }

    /**
     * Parses the configuration provided by the setupDataContainer
     * into the config field, which is a Jena DefaultModel. Also
     * pre-parses the label and the title property.
     *
     * Also determines the config resource (the resource with
     * rdf:type vis:config) and sets the field configBase
     * accordingly.
     *
     * TODO: Deal with other config resources in the config file.
     *
     * @param setupDataContainer
     */
    public void initialize(SetupDataContainer setupDataContainer) {
        // in case we reload the config, start from an empty inputModel
        config.removeAll();
        config.read(setupDataContainer.getGraphConfigInputStream(), null, "Turtle");

        ResIterator iter = config.listSubjectsWithProperty(RDF.type, VIS.Config);
        if(iter.hasNext()) {
            configBase = iter.next();
        }

        parseLabelProperty();
        parseTitleProperties();
        //parseDataSource();
    }


    /**
     * The labelProperty will be used to label nodes in the final
     * visualisation.
     *
     * @return The RDF property used to label nodes
     */
    public Property getLabelProperty() {
        return labelProperty;
    }

    public List<Property> getTitleProperties() {
        return titleProperties;
    }

    public List<Property> getExpandOutgoingProperties() {
        NodeIterator iter = config.listObjectsOfProperty(configBase, VIS.expandOutgoingProperties);
        List<Property> properties = new ArrayList<>();
        if (iter.hasNext()) {
            RDFNode listNode = iter.nextNode();
            properties = getAsPropertyList(listNode);
        }
        return properties;
    }

    public List<Property> getExpandIncomingProperties() {
        NodeIterator iter = config.listObjectsOfProperty(configBase, VIS.expandIncomingProperties);
        List<Property> properties = new ArrayList<>();
        if (iter.hasNext()) {
            RDFNode listNode = iter.nextNode();
            properties = getAsPropertyList(listNode);
        }
        return properties;
    }

    public Boolean showLabels() {
        return fetchBoolean(VIS.showLabels, true);
    }

    /**
     * @deprecated The ontology data is set via the SetupDataContainer
     */
    @Deprecated
    public String getOntologySource() {
        return getStringConfigValue(VIS.ontologySource);
    }

    /**
     * @deprecated The input data source is set via the SetupDataContainer
     */
    @Deprecated
    public String getDataSource() {
        return dataSource;
    }

    /**
     * This method will return the default shape.
     *
     * @return The default shape
     */
    public String getDefaultShape() {
        return getStringConfigValue(VIS.defaultShape);
    }

    /**
     * This is a convencience function that reads a property from the
     * config field, retrieves the (first) value, and converts it to
     * a boolean. In case the property is not used in the config (or
     * does not point to a boolean) the default value is used.
     *
     * @param property The property that should be queried
     * @param defaultValue The default value in case the property
     *                     is not used (or does not point to a
     *                     boolean).
     *
     * @return A boolean for the property provided
     */
    private Boolean fetchBoolean(Property property, Boolean defaultValue) {
        RDFNode test = getConfigValue(property);
        if (test != null && test.isLiteral()) {
            return test.asLiteral().getBoolean();
        }
        else {
            return defaultValue;
        }
    }

    public Boolean showLiterals() {
        return fetchBoolean(VIS.showLiterals, true);
    }

    public Boolean showClasses() {
        return fetchBoolean(VIS.showClasses, true);
    }

    public Boolean allowExpansion() {
        return fetchBoolean(VIS.allowExpansion, false);
    }

    public Integer getMaxLabelLength() {
        return getIntegerConfigValue(VIS.maxLabelLength);
    }

    public Boolean isExpansionLimitedToProperties() {
        return fetchBoolean(VIS.limitExpansionToProperties, false);
    }

    /**
     * Convenience function that reads the value of a property in the
     * config and converts it to a string.
     *
     * @param property
     * @return the value a property points to, converted to a string
     *         or null as failure
     */
    private String getStringConfigValue(Property property) {
        NodeIterator iter = config.listObjectsOfProperty(configBase, property);
        if (iter.hasNext()) {
            return iter.next().toString();
        }
        else {
            return null;
        }
    }

    /**
     * Convenience function that reads the value of a property in the
     * config and converts it to an integer.
     *
     * @param property
     * @return the value a property points to, converted to an integer
     *         or null as failure
     */
    private Integer getIntegerConfigValue(Property property) {
        NodeIterator iter = config.listObjectsOfProperty(configBase, property);
        if (iter.hasNext()) {
            return iter.next().asLiteral().getInt();
        }
        else {
            return null;
        }
    }

    private RDFNode getConfigValue(Property p) {
        NodeIterator iter = config.listObjectsOfProperty(configBase, p);
        if (iter.hasNext()) {
            return iter.next();
        }
        else {
            return null;
        }
    }

    private void parseLabelProperty() {
        RDFNode r = getConfigValue(VIS.labelProperty);
        if (r != null) {
            Property prop = r.as(Property.class);
            labelProperty = prop;
        }
        else {
            // default value
            labelProperty = RDFS.label;
        }
    }

    private List<Property> getAsPropertyList(RDFNode listNode) {
        List<Property> list = new ArrayList<Property>();
        RDFList rdflist = listNode.as(RDFList.class);

        ExtendedIterator<RDFNode> ni = rdflist.iterator();
        while(ni.hasNext()) {
            RDFNode node = ni.next();
            list.add(node.as(Property.class));
        }

        return list;
    }

    private List<String> getAsStringList(RDFNode listNode) {
        List<String> list = new ArrayList<>();
        RDFList rdflist = listNode.as(RDFList.class);

        ExtendedIterator<RDFNode> ni = rdflist.iterator();
        while(ni.hasNext()) {
            RDFNode node = ni.next();
            list.add(node.toString());
        }

        return list;
    }

    public List<String> getRuleSets() {
        NodeIterator iter = config.listObjectsOfProperty(configBase, VIS.ruleSets);
        if (iter.hasNext()) {
            RDFNode listNode = iter.nextNode();
            return getAsStringList(listNode);
        }
        else {
            return new ArrayList<>();
        }
    }

    private void parseTitleProperties() {
        NodeIterator iter = config.listObjectsOfProperty(configBase, VIS.titleProperties);
        if (iter.hasNext()) {
            RDFNode listNode = iter.nextNode();
            titleProperties = getAsPropertyList(listNode);
        }
        else {
            titleProperties = new ArrayList<Property>();
        }
    }

    private void parseDataSource() {
        dataSource = getConfigValue(VIS.dataSource).toString();
    }

}
