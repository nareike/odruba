package odruba.service;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ModelTools {

    /**
     * Tests if a resource has any (literal) value for a given
     * property wrt a model. I.e. the function for model m,
     * resource r and property p will return true iff there is
     * a triple (r, p, v) with literal v in the model m.
     *
     * @param model
     * @param r
     * @param p
     * @return
     */
    public Boolean hasValue(Model model, Resource r, Property p) {
        StmtIterator iter = model.listStatements(r, p, (RDFNode) null);
        return iter.hasNext();
    }

    /**
     * There seems to be a bug extracting the namespace of URIs that
     * end with a number. This is a workaround for this bug.
     *
     * @param url
     * @return
     */
    public String namespaceHack(String url) {
        if (url != null) {
            int slashIndex = url.lastIndexOf('/');
            int hashIndex = url.lastIndexOf('#');
            return url.substring(0, Math.max(slashIndex, hashIndex) + 1);
        }
        else {
            return null;
        }
    }

    /**
     * Tests a 'flag like' property p for a resource r wrt a model m.
     * Will return true iff there is a triple (r, p, b) in the
     * model m where b can be parse to True.
     *
     * @param model
     * @param r
     * @param p
     * @return
     */
    public Boolean testFlag(Model model, Resource r, Property p) {
        StmtIterator iter = model.listStatements(r, p, (RDFNode) null);
        if (iter.hasNext()) {
            Statement statement = iter.next();
            RDFNode object = statement.getObject();
            if (object.isLiteral()) {
                return statement.getObject().asLiteral().getBoolean();
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public String getValue(Model model, Resource r, Property p) {
        StmtIterator statement = model.listStatements(r, p, (RDFNode) null);
        RDFNode object = statement.next().getObject();
        if (object.isResource()) {
            return object.toString();
        }
        else if (object.isLiteral()) {
            return object.asLiteral().getString();
        }
        else {
            return "";
        }
    }

    public List<String> getValues(Model model, Resource r, Property p) {
        StmtIterator statementIter = model.listStatements(r, p, (RDFNode) null);
        List<String> values = new ArrayList<String>();

        while (statementIter.hasNext()) {
            Statement statement = statementIter.next();
            RDFNode object = statement.getObject();

            if (object.isResource()) {
                values.add(object.toString());
            }
            else if (object.isLiteral()) {
                values.add(object.asLiteral().getString());
            }
        }
        return values;
    }

    /**
     * Return all resources that are referenced in a model, i.e.
     * every resource that is used as a subject or on object
     * in any triple of the given model.
     *
     * @param model the model
     * @return a set of resources
     */
    public Set<Resource> getResourcesInModel(Model model) {
        Set<Resource> in = model.listSubjects().toSet();
        NodeIterator obiter = model.listObjects();
        while (obiter.hasNext()) {
            RDFNode nd = obiter.next();
            if (nd.isResource()) {
                in.add(nd.asResource());
            }
        }
        return in;
    }

    /**
     * Will try to fetch a value for a resource r that is connected
     * by a chain of properties p1...pn in the model. That is, there
     * exist triples in the model m:
     *    (r, p1, o1) (o1, p2, o2) ... (on-1, pn, on)
     * When this chain cannot be built, an empty string is returned.
     * Also, if there are multiple possible objects at any point of
     * the chain, the first one will be picked.
     *
     * The idea of this function is to help parsing structured
     * values constructed with blank nodes.
     *
     * @param model
     * @param r
     * @param props A list of properties
     * @return
     */
    public String getChainedPropertyValue(Model model, Resource r, Property... props) {
        Resource o = r;

        for(Property p : props) {
            StmtIterator iter = model.listStatements(o, p, (RDFNode) null);
            if (iter.hasNext()) {
                Statement statement = iter.nextStatement();
                RDFNode object = statement.getObject();
                if (object.isResource()) {
                    o = object.asResource();
                }
                else {
                    return object.asLiteral().getString();
                }
            }
        }

        return null;
    }

    public Set<Resource> rdfTypes(Model model, Resource r) {
        /* TODO:
         * Use code like this
         *     subject.as(OntResource.class).listRDFTypes(false);
         * To do so, the subject must by tied to an OntModel
         */
        HashSet<Resource> s = new HashSet<Resource>();
        StmtIterator iter = model.listStatements(r, RDF.type, (RDFNode) null);
        while(iter.hasNext()) {
            Statement statement = iter.nextStatement();
            s.add(statement.getObject().asResource());
        }
        return s;
    }
}
