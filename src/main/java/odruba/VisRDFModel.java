package odruba;

import org.apache.jena.rdf.model.*;

import java.util.HashMap;
import java.util.Map;

public class VisRDFModel {

    private Map<Statement, Provenance> statementProvenance;
    private Map<RDFNode, Provenance> nodeProvenance;
    private Model visModel;

    public VisRDFModel() {
        statementProvenance = new HashMap<>();
        nodeProvenance = new HashMap<>();
        visModel = ModelFactory.createDefaultModel();
    }

    public Model getVisModel() {
        return visModel;
    }

    public Map<Statement, Provenance> getStatementProvenance() {
        return statementProvenance;
    }

    public void setStatementProvenance(Map<Statement, Provenance> statementProvenance) {
        this.statementProvenance = statementProvenance;
    }

    public Map<RDFNode, Provenance> getNodeProvenance() {
        return nodeProvenance;
    }

    public void setNodeProvenance(Map<RDFNode, Provenance> nodeProvenance) {
        this.nodeProvenance = nodeProvenance;
    }

    public Provenance getProvenance(RDFNode node) {
        return nodeProvenance.get(node);
    }

    public Provenance getProvenance(Statement statement) {
        return statementProvenance.get(statement);
    }

    public void setProvenance(RDFNode node, Provenance provenance) {
        nodeProvenance.put(node, provenance);
    }

    public void setProvenance(Statement statement, Provenance provenance) {
        statementProvenance.put(statement, provenance);
    }

    public void add(Statement statement) {
        visModel.add(statement);
    }

    public void add(Statement statement, Provenance provenance) {
        visModel.add(statement);
        setProvenance(statement, provenance);
    }

    public void add(Model model, Provenance provenance) {
        visModel.add(model);
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            setProvenance(iter.next(), provenance);
        }
    }

    public VisRDFModel getDiff(VisRDFModel other) {
        Model diff = ModelFactory.createDefaultModel();
        StmtIterator iter = other.getVisModel().listStatements();
        while(iter.hasNext()) {
            Statement statement = iter.nextStatement();
            if (!visModel.contains(statement)) {
                diff.add(statement);
            }
        }
        VisRDFModel diffDesc = new VisRDFModel();
        diffDesc.add(diff, Provenance.GIVEN);
        return diffDesc;
    }

    public void add(VisRDFModel other) {
        visModel.add(other.getVisModel());
    }

}
