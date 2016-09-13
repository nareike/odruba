package odruba.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This component basically is a POJO to hold a configuration. It is
 * primarily used to exchange data with the front end. When Odruba
 * is started from the command line, the setupDataContainer will be
 * initialized with the data specified in the application.yaml
 */
@Component
public class SetupDataContainer {

    private String graphConfig = "";
    private String inputData = "";
    private String ontologyData = "";
    private List<RulesetData> rulesetData = new ArrayList<RulesetData>();

    // --- S E T T E R S ---

    public void setGraphConfig(String graph_config) {
        this.graphConfig = graph_config;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public void setOntologyData(String ontologyData) {
        this.ontologyData = ontologyData;
    }

    public void setRulesetData(List<RulesetData> rulesetData) {
        this.rulesetData = rulesetData;
    }

    // --- G E T T E R S ---

    public String getGraphConfig() {
        return graphConfig;
    }

    public String getInputData() {
        return inputData;
    }

    public String getOntologyData() {
        return ontologyData;
    }

    public List<RulesetData> getRulesetData() {
        return rulesetData;
    }

    /**
     * This is a convenience function that converts the graphConfig string
     * to a ByteArrayInputStream
     * @return graphConfig (as ByteArrayInputStream)
     */
    @JsonIgnore
    public InputStream getGraphConfigInputStream() {
        return new ByteArrayInputStream(graphConfig.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * This is a convenience function that converts the inputData string
     * to a ByteArrayInputStream
     * @return inputData (as ByteArrayInputStream)
     */
    @JsonIgnore
    public InputStream getInputDataInputStream() {
        return new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * This is a convenience function that converts the ontologyData string
     * to a ByteArrayInputStream
     * @return ontologyData (as ByteArrayInputStream)
     */
    @JsonIgnore
    public InputStream getOntologyDataInputStream() {
        return new ByteArrayInputStream(ontologyData.getBytes(StandardCharsets.UTF_8));
    }

    public void update(SetupDataContainer data) {
        this.inputData = data.getInputData();
        this.ontologyData = data.getOntologyData();
        this.graphConfig = data.getGraphConfig();
        this.rulesetData = data.getRulesetData();
    }
}
