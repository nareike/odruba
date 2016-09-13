package odruba.controller;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import odruba.Provenance;
import odruba.config.ModelHerder;
import odruba.config.SetupDataContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@CrossOrigin
public class RDFController {

    @Autowired
    private ModelHerder modelHerder;

    @Autowired
    private SetupDataContainer setupDataContainer;

    @RequestMapping(value = "/vis/explain", method = RequestMethod.POST, consumes = TEXT_PLAIN_VALUE)
    //public String explainTripel(@RequestBody String body) {
    public String explainTripel(HttpServletRequest request) {
        Model input = ModelFactory.createDefaultModel();
        try {
            input.read(request.getInputStream(), null, "TURTLE");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Statement statement = input.listStatements().nextStatement();

        String resultString = new String();
        Provenance prov = modelHerder.getProvenance(statement);

        switch (prov) {
            case GIVEN:
                resultString += "This fact was included directly.";
                break;
            case ONTOLOGY:
                resultString += "This fact was infered from the ontology / background knowledge.";
                break;
            case REASONER:
                for(InfModel infModel : modelHerder.getInferenceModelList()) {
                    for (final Iterator<Derivation> id = infModel.getDerivation(statement); id.hasNext(); ) {
                        final RuleDerivation deriv = (RuleDerivation) id.next();
                        Triple conclusion = deriv.getConclusion();
                        List<Triple> matches = deriv.getMatches();
                        Rule rule = deriv.getRule();
                        resultString += "Rule \"" + rule.getName() + "\" concluded: " + tripleToString(conclusion) + "\n";
                        for (int i = 0; i < matches.size(); i++) {
                            Triple triple =  matches.get(i);
                            resultString += "    [Fact] " + tripleToString(triple) + "\n";
                        }
                    }
                }
                break;
        }
        return resultString;
    }

    public String nodeToString(Model m, Node n) {
        if (n.isLiteral()) {
            return n.getLiteral().toString();
        }
        else if (n.isBlank()) {
            return "_:" + n.getBlankNodeId();
        }
        else if (n.isURI()) {
            String shortURI = m.shortForm(n.getURI());
            if (shortURI.equals(n.getURI())) {
                return "<" + n.getURI() + ">";
            }
            else {
                return shortURI;
            }
        }
        return "<?>";
    }

    public String tripleToString(Triple triple) {
        Model miniModel = ModelFactory.createDefaultModel();
        miniModel.setNsPrefixes(modelHerder.getNsPrefixMap());
        String subj = nodeToString(miniModel, triple.getSubject());
        String pred = nodeToString(miniModel, triple.getPredicate());
        String obje = nodeToString(miniModel, triple.getObject());
        return subj + " " + pred + " " + obje;
    }

    @RequestMapping("/rdf/graph")
    public String getRDFConstructedModel() {
        StringWriter stringWriter = new StringWriter();
        Model model = modelHerder.getVisRDFModel().getVisModel();
        model.setNsPrefixes(modelHerder.getNsPrefixMap());
        model.write(stringWriter, "TURTLE");
        return stringWriter.toString();
    }

    @RequestMapping("config")
    public SetupDataContainer getConfig() {
        return setupDataContainer;
    }

    /*
    @RequestMapping(value = "/rdf/resource",
                    method = RequestMethod.POST,
                    consumes = TEXT_PLAIN_VALUE,
                    produces = "text/turtle"
    )
    public String getRDFFromResource(@RequestBody String body) {
        Resource resource = ResourceFactory.createResource(body);
        representationBuilder.modelFromResource(resource);
        return getRDFConstructedModel();
    }
    */

}
