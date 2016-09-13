package odruba.config;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import odruba.Provenance;
import odruba.VisRDFModel;

import java.util.*;

@Component
public class ModelHerder {

    private GraphConfiguration graphConfiguration;
    private SetupDataContainer setupDataContainer;

    // Models as data containers without any reasoning or inference
    private Model inputModel;
    private Model ontologyData;
    private Model union_stub;

    // Ontology model build from the inputModel and the ontologyData
    private OntModel union;

    private VisRDFModel visRDFModel;

    // ruleSets as field are probably not needed since they can also
    // be extracted from the InfModel
    private List<List<Rule>> ruleSets;
    private List<InfModel> infModelList;

    // convenience leftover stuff, to be removed
    private InfModel temp;
    private InfModel infModel;

    private Map nsPrefixMap;

    @Autowired
    public ModelHerder(GraphConfiguration graphConfiguration, SetupDataContainer setupDataContainer) {
        this.graphConfiguration = graphConfiguration;
        this.setupDataContainer = setupDataContainer;
        nsPrefixMap = new HashMap<>();
        setInputModel();
    }

    public void setInputModel() {
        closeAllModels();
        inputModel = ModelFactory.createDefaultModel();
        inputModel.read(setupDataContainer.getInputDataInputStream(), null, "Turtle");

        nsPrefixMap.putAll(inputModel.getNsPrefixMap());
        loadData();
    }

    public void setInputModel(Model newVisModel) {
        // since there are more models built upon the inputModel,
        // each time the inputModel gets that, the whole stack has
        // to be updated
        closeAllModels();

        inputModel = ModelFactory.createDefaultModel();
        if (newVisModel != null) {
            inputModel.add(newVisModel);
        }

        nsPrefixMap.putAll(inputModel.getNsPrefixMap());
        loadData();
    }

    public void setInputModel(String location) {
        closeAllModels();
        inputModel = ModelFactory.createDefaultModel();
        inputModel.read(location);

        nsPrefixMap.putAll(inputModel.getNsPrefixMap());
        loadData();
    }

    public VisRDFModel getVisRDFModel() {
        return visRDFModel;
    }

    public void setVisRDFModel(VisRDFModel visRDFModel) {
        this.visRDFModel = visRDFModel;
    }

    public Model getInputModel() {
        return inputModel;
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public Model getUnion() {
        return union;
    }

    public List<List<Rule>> getRuleSets() {
        return ruleSets;
    }

    public List<InfModel> getInfModelList() {
        return infModelList;
    }

    public Map getNsPrefixMap() {
        return nsPrefixMap;
    }

    public InfModel getTemp() {
        return temp;
    }

    public List<InfModel> getInferenceModelList() {
        return infModelList;
    }

    /**
     * @deprecated Use setInputModel(null)
     */
    @Deprecated
    public void setEmptyVisModel() {
        closeAllModels();
        inputModel = ModelFactory.createDefaultModel();
        loadData();
    }

    public void addToVisModel(Model extraTriples) {
        Model temp = ModelFactory.createDefaultModel();
        temp.add(inputModel);
        temp.add(extraTriples);
        setInputModel(temp);
    }

    @CacheEvict(value = "nodes", allEntries = true)
    public void reloadData() {
        setInputModel();
    }

    private void closeAllModels() {
        if (inputModel != null && !inputModel.isClosed()) {
            inputModel.close();
        }
        if (ontologyData != null && !ontologyData.isClosed()) {
            ontologyData.close();
        }
        if (union_stub != null && !union_stub.isClosed()) {
            union_stub.close();
        }
        if (union != null && !union.isClosed()) {
            union.close();
        }
        if (infModel != null && !infModel.isClosed()) {
            infModel.close();
        }
        if (temp != null && !temp.isClosed()) {
            temp.close();
        }
    }

    /**
     * @deprecated Use either setInputModel(graphConfiguration.getDataSource()) directly
     * or use reloadData()
     */
    @Deprecated
    private void loadVisModel() {
        setInputModel(graphConfiguration.getDataSource());
        /*
        inputModel = ModelFactory.createDefaultModel();
        inputModel.read(graphConfiguration.getDataSource());
        */
    }

    private void loadData() {
        ontologyData = ModelFactory.createDefaultModel();
        ontologyData.read(setupDataContainer.getOntologyDataInputStream(), null, "Turtle");

        union_stub = ModelFactory.createUnion(inputModel, ontologyData);
        union = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF, union_stub);

        setUpInferenceModels();
    }

    private void setUpInferenceModels() {
        ruleSets = new ArrayList<>();
        infModelList = new ArrayList<>();

        //List <String> ruleURIs = graphConfiguration.getRuleSets();

        for (int i = 0; i < setupDataContainer.getRulesetData().size(); i++) {
            RulesetData rulesetData = setupDataContainer.getRulesetData().get(i);
            List<Rule> ruleSet = Rule.parseRules(Rule.rulesParserFromReader(rulesetData.getDataStream()));
            ruleSets.add(ruleSet);

            GenericRuleReasoner reasoner = new GenericRuleReasoner(ruleSet);
            reasoner.setDerivationLogging(true);

            InfModel infModel;

            if (i == 0) {
                infModel = ModelFactory.createInfModel(reasoner, union);
                infModelList.add(infModel);
            }
            else {
                infModel = ModelFactory.createInfModel(reasoner, infModelList.get(i-1));
                infModelList.add(infModel);
            }
        }

        /*
        for (int i = 0; i < ruleURIs.size(); i++) {
            String ruleSetURL = ruleURIs.get(i);
            List<Rule> ruleSet = Rule.rulesFromURL(ruleSetURL);
            ruleSets.add(ruleSet);

            GenericRuleReasoner reasoner = new GenericRuleReasoner(ruleSet);
            reasoner.setDerivationLogging(true);

            InfModel infModel;

            if (i == 0) {
                infModel = ModelFactory.createInfModel(reasoner, union);
                infModelList.add(infModel);
            }
            else {
                infModel = ModelFactory.createInfModel(reasoner, infModelList.get(i-1));
                infModelList.add(infModel);
            }
        }
        */

        infModel = infModelList.get(infModelList.size() - 1);
    }

    public Provenance getProvenance(Statement s) {
        if (inputModel != null && inputModel.contains(s)) {
            return Provenance.GIVEN;
        }
        else if (union.contains(s)) {
            return Provenance.ONTOLOGY;
        }
        else {
            return Provenance.REASONER;
        }
    }

    public Provenance getProvenance(RDFNode n) {
        if (inputModel != null && inputModel.containsResource(n)) {
            return Provenance.GIVEN;
        }
        else if (union.containsResource(n)) {
            return Provenance.ONTOLOGY;
        }
        else {
            return Provenance.REASONER;
        }
    }

}
