package odruba;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import odruba.config.GraphConfiguration;
import odruba.config.ModelHerder;
import odruba.service.ModelTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VisRDFBuilder {

    private ModelHerder modelHerder;
    private GraphConfiguration graphConfiguration;

    private VisRDFModel visRDFModel;

    private Model model;
    private InfModel infModel;

    @Autowired
    private ModelTools modelTools;

    @Autowired
    public VisRDFBuilder(ModelHerder modelHerder, GraphConfiguration graphConfiguration) {
        this.modelHerder = modelHerder;
        this.graphConfiguration = graphConfiguration;

        model = modelHerder.getInputModel();
        infModel = modelHerder.getInfModel();
    }

    public Set<Resource> rdfTypes(Resource r) {
        return modelTools.rdfTypes(infModel, r);
    }

    private Boolean showNode(RDFNode n) {
        if (n.isResource()) {
            Resource r = n.inModel(infModel).asResource();
            Set<Resource> types = rdfTypes(n.asResource());
            if (r.hasProperty(VIS.hidden)) {
                return !modelTools.testFlag(infModel, r, VIS.hidden);
            }
            if (types.contains(VIS.Hidden)) {
                return false;
            }
            return graphConfiguration.showClasses() || !types.contains(RDFS.Class);
        }
        else if (n.isLiteral()) {
            return graphConfiguration.showLiterals();
        }
        else {
            return false;
        }
    }

    private Boolean showProperty(Property p) {
        Set<Resource> types = rdfTypes(p);
        return !modelTools.testFlag(infModel, p, VIS.hidden);
    }

    private Boolean showStatement(Statement statement) {
        Resource s = statement.getSubject();
        RDFNode o = statement.getObject();
        if(o.isResource()) {
            if (s.equals(o)) {
                return false;
            }
        }
        return true;
    }

    private void parseStatement(Statement statement, Provenance provenance) {
        Resource subject = statement.getSubject();
        RDFNode  object = statement.getObject();

        if (showNode(subject) && showNode(object) && showProperty(statement.getPredicate())
                && showStatement(statement)) {
            visRDFModel.add(statement, provenance);
        }
    }

    private List<Statement> getInferredStatementsForResource(Resource subject) {
        //System.out.println("    > getInferredStatementsForResource(" + subject.getURI() + ")");
        long startTime = System.nanoTime();
        List<Statement> inferredStatements = new ArrayList<>();

        /*
        HashSet<Property> expandOut = new HashSet<>(graphConfiguration.getExpandOutgoingProperties());
        HashSet<Property> expandIn = new HashSet<>(graphConfiguration.getExpandIncomingProperties());
        */

        if (graphConfiguration.isExpansionLimitedToProperties()) {
            /*
            // NOT REALLY FASTER
            ExtendedIterator<Statement> resultOut = infModel
                    .listStatements(subject, null, (RDFNode) null)
                    .filterKeep(
                            s -> expandOut.contains(s.getPredicate())
                    );

            ExtendedIterator<Statement> resultIn = infModel
                    .listStatements(null, null, subject)
                    .filterKeep(
                            s -> expandIn.contains(s.getPredicate())
                    );

            inferredStatements.addAll(resultOut.toList());
            inferredStatements.addAll(resultIn.toList());
            */
            for (Property expandOn : graphConfiguration.getExpandOutgoingProperties()) {
                inferredStatements.addAll(infModel.listStatements(subject, expandOn, (RDFNode) null).toSet());
            }
            for (Property expandOn : graphConfiguration.getExpandIncomingProperties()) {
                inferredStatements.addAll(infModel.listStatements(null, expandOn, subject).toSet());
            }
        } else {
            inferredStatements.addAll(infModel.listStatements(subject, null, (RDFNode) null).toSet());
            inferredStatements.addAll(infModel.listStatements(null, null, subject).toSet());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("    > Duration: " + duration / 1000000);

        return inferredStatements;
    }

    private List<Statement> getInferredStatementsBetweenTwoResourceSets(Set<Resource> set1, Set<Resource> set2) {
        //System.out.println("    > getInferredStatementsBetweenTwoResourceSets");
        long startTime = System.nanoTime();

        ExtendedIterator<Statement> result = infModel
                .listStatements(null, null, (Resource) null)
                .filterKeep(
                        s -> (set1.contains(s.getSubject()) && set2.contains(s.getObject()))
                                ||
                             (set2.contains(s.getSubject()) && set1.contains(s.getObject()))
                );


        List<Statement> inferredStatements = result.toList();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("    > Duration: " + duration / 1000000);

        return inferredStatements;
    }

    private List<Statement> getInferredStatementsBetweenResources(Set<Resource> resourceSet) {
        //System.out.println("    > getInferredStatementsBetweenResources");
        long startTime = System.nanoTime();

        ExtendedIterator<Statement> result = infModel
                .listStatements(null, null, (Resource) null)
                .filterKeep(
                        s -> resourceSet.contains(s.getSubject()) && resourceSet.contains(s.getObject())
                );

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("    > Duration: " + duration / 1000000);

        return result.toList();
    }

    public VisRDFModel modelFromResource(Resource resource) {
        return modelFromResource(resource, true);
    }

    public VisRDFModel modelFromResource(Resource resource, Boolean expand) {
        //modelHerder.setEmptyVisModel();
        //model = modelHerder.inputModel;
        infModel = modelHerder.getInfModel();

        List<Statement> newStatements = getInferredStatementsForResource(resource);
        List<Statement> woBlank = newStatements.stream()
                .filter((Statement s) -> !s.getObject().isAnon())
                .collect(Collectors.toList());

        Model newModel = ModelFactory.createDefaultModel();
        newModel.add(woBlank);

        newModel.add(resource, VIS.glow, "#FFFF00");
        //newModel.add(resource, VIS.size, "30");
        //newModel.add(resource, VIS.fontSize, "30");
        //modelHerder.setInputModel(newModel);
        //newModel.close();

        return buildRDFDescription(newModel, expand);
    }

    public VisRDFModel connectingModel(Model model1, Model model2) {
        Set<Resource> inModel1 = modelTools.getResourcesInModel(model1);
        Set<Resource> inModel2 = modelTools.getResourcesInModel(model2);

        List<Statement> newStatements = getInferredStatementsBetweenTwoResourceSets(inModel1, inModel2);
        Model newModel = ModelFactory.createDefaultModel();
        newModel.add(newStatements);
        return buildRDFDescription(newModel, false);
    }

    public VisRDFModel buildRDFDescription(Model model) {
        return buildRDFDescription(model, true);
    }

    public VisRDFModel buildRDFDescription(Model model, Boolean expand) {
        visRDFModel = new VisRDFModel();

        //model = modelHerder.inputModel;
        infModel = modelHerder.getInfModel();
        Model onto = modelHerder.getUnion();

        Set<Resource> inModel = modelTools.getResourcesInModel(model);

        StmtIterator iter = model.listStatements();

        while(iter.hasNext()) {
            Statement statement = iter.next();
            parseStatement(statement, Provenance.GIVEN);
        }

        Model inferredStatements = ModelFactory.createDefaultModel();

        //System.out.println("  > ... expanding model");
        long startTime = System.nanoTime();

        if (expand && graphConfiguration.allowExpansion()) {
            // Expand the resources of the model
            for (Resource subject : inModel) {
                inferredStatements.add(getInferredStatementsForResource(subject));
            }
            // Closure, add statements about the resources now in the model
            inModel.addAll(modelTools.getResourcesInModel(inferredStatements));
            inferredStatements.add(getInferredStatementsBetweenResources(inModel));
        }
        else {
            inferredStatements.add(getInferredStatementsBetweenResources(inModel));
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("  > Duration: " + duration / 1000000);

        iter = inferredStatements.listStatements();

        //System.out.println("  > ... parsing statements");
        startTime = System.nanoTime();

        while(iter.hasNext()) {
            Statement infStatement = iter.nextStatement();
            RDFNode infObject = infStatement.getObject();
            // include nodes from background graphs?
            //if (!inputModel.contains(infStatement) && infObject.isResource() && inModel.contains(infObject.asResource())) {
            if (!model.contains(infStatement) && infObject.isResource())  {
                if (!onto.contains(infStatement)) {
                    parseStatement(infStatement, Provenance.ONTOLOGY);
                }
                else {
                    parseStatement(infStatement, Provenance.REASONER);
                }
            }
        }

        endTime = System.nanoTime();
        duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        //System.out.println("  > Duration: " + duration / 1000000);

        //modelHerder.visRDFModel = visRDFModel;
        return visRDFModel;
    }

}
