package odruba.controller;

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import odruba.*;
import odruba.config.SetupDataContainer;
import odruba.config.ModelHerder;
import odruba.config.GraphConfiguration;
import odruba.pojo.vis.Graph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.*;

@RestController
@CrossOrigin
public class VisController {

    @Autowired
    private ModelHerder modelHerder;

    @Autowired
    private GraphConfiguration graphConfiguration;

    @Autowired
    private SetupDataContainer setupDataContainer;

    @Autowired
    private VisRDFBuilder visRDFBuilder;

    @Autowired
    private VisGraphBuilder visGraphBuilder;

    @RequestMapping("/vis/setup")
    public void testJson(@RequestBody SetupDataContainer data) {
    }

    @RequestMapping("/vis/test")
    public Rule testTest() {
        /*
        Model model = ModelFactory.createDefaultModel();
        Model proxied =(Model) Proxy.newProxyInstance(Application.class.getClassLoader(),
                        model.getClass().getInterfaces(), new ProxyModel(model));

        proxied.add(modelHerder.visRDFModel.getVisModel());
        */

        //return "TestController";
        System.out.println(modelHerder.getRuleSets().get(0).get(0).getHead().toString());
        return modelHerder.getRuleSets().get(0).get(0);
    }

    @RequestMapping("/vis/graph")
    public Graph getVISgraph() {
        VisRDFModel visRDFModel;
        /*
        if (modelHerder.inputModel == null) {
            visRDFModel = modelHerder.visRDFModel;
        }
        else {
            visRDFModel = visRDFBuilder.buildRDFDescription();
            modelHerder.inputModel = null;
            modelHerder.visRDFModel = visRDFModel;
        }
        */
        if (modelHerder.getVisRDFModel() == null) {
            visRDFModel = visRDFBuilder.buildRDFDescription(modelHerder.getInputModel());
            modelHerder.setVisRDFModel(visRDFModel);
        }
        else {
            visRDFModel = modelHerder.getVisRDFModel();
        }
        return visGraphBuilder.model2json(visRDFModel);
    }

    @RequestMapping("/vis/expand")
    public void expand() {
        modelHerder.setInputModel(modelHerder.getVisRDFModel().getVisModel());
    }

    @RequestMapping(value = "/vis/add", method = RequestMethod.POST, consumes = "text/turtle")
    public void addToVisModel(@RequestBody String body) {
        addToVisModel(body, "TURTLE");
    }

    public void addToVisModel(String body, String format) {
        Model submitted = ModelFactory.createDefaultModel();
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        submitted.read(stream, null, format);

        modelHerder.addToVisModel(submitted);
        submitted.close();
    }

    @RequestMapping(value = "/vis/resource/zoom", method = RequestMethod.POST, consumes = TEXT_PLAIN_VALUE)
    public Graph getZoomedModelFromResource(@RequestBody String body) {
        Resource resource = ResourceFactory.createResource(body);
        VisRDFModel resDesc = visRDFBuilder.modelFromResource(resource);
        modelHerder.setVisRDFModel(resDesc);
        return visGraphBuilder.model2json(resDesc);
    }

    @RequestMapping(value = "/vis/resource/simple", method = RequestMethod.POST, consumes = TEXT_PLAIN_VALUE)
    public Graph getSimpleModelFromResource(@RequestBody String body) {
        Resource resource = ResourceFactory.createResource(body);
        VisRDFModel resDesc = visRDFBuilder.modelFromResource(resource, false);
        modelHerder.setVisRDFModel(resDesc);
        return visGraphBuilder.model2json(resDesc);
    }

    @RequestMapping(value = "/vis/resource/simple/add", method = RequestMethod.POST, consumes = TEXT_PLAIN_VALUE)
    public Graph addSimpleModelFromResource(@RequestBody String body) {
        Resource resource = ResourceFactory.createResource(body);
        VisRDFModel resDesc = visRDFBuilder.modelFromResource(resource, false);
        VisRDFModel diff = modelHerder.getVisRDFModel().getDiff(resDesc);
        modelHerder.getVisRDFModel().add(diff);
        return visGraphBuilder.model2json(resDesc);
    }

    @RequestMapping(value = "/vis/resource", method = RequestMethod.POST, consumes = TEXT_PLAIN_VALUE)
    public Graph getModelFromResource(@RequestBody String body) {
        Resource resource = ResourceFactory.createResource(body);
        VisRDFModel resDesc = visRDFBuilder.modelFromResource(resource);
        VisRDFModel connecting = visRDFBuilder.connectingModel(resDesc.getVisModel(), modelHerder.getVisRDFModel().getVisModel());

        VisRDFModel union = visRDFBuilder.buildRDFDescription(resDesc.getVisModel().add(connecting.getVisModel()), false);

        VisRDFModel diff = modelHerder.getVisRDFModel().getDiff(union);
        modelHerder.getVisRDFModel().add(diff);
        return visGraphBuilder.model2json(diff);
    }

    @RequestMapping(value = "/vis/submit", method = RequestMethod.POST)
    public void submit(@RequestBody SetupDataContainer data) {
        setupDataContainer.update(data);
        graphConfiguration.initialize();
        modelHerder.setVisRDFModel(null);
        modelHerder.reloadData();
        /*
        Model submitted = ModelFactory.createDefaultModel();
        submitted.read(data.getInputDataInputStream(), null, "TURTLE");

        setupDataContainer.setInputData(data.getInputData());

        modelHerder.setInputModel(submitted);
        VisRDFModel visRDFModel = visRDFBuilder.buildRDFDescription(submitted);
        modelHerder.setVisRDFModel(visRDFModel);
        submitted.close();
        */
    }

    /**
     * Trigger a reload of all data. Useful when changing local configuration or ontology
     * files.
     */
    @RequestMapping("/vis/reload")
    public void reload() throws IOException {
        graphConfiguration.loadInitialConfig();
        graphConfiguration.initialize();
        modelHerder.setVisRDFModel(null);
        modelHerder.reloadData();
    }

}
