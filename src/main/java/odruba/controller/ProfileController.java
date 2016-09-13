package odruba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import odruba.config.GraphConfiguration;
import odruba.config.ModelHerder;

@RestController
@CrossOrigin
public class ProfileController {

    @Autowired
    private GraphConfiguration graphConfiguration;

    @Autowired
    private ModelHerder modelHerder;

    @RequestMapping(value = "profile/set")
    public String setProfile(@RequestParam(value="uri") String uri) {
        if (graphConfiguration.setGraphConfigFile(uri)) {
            modelHerder.setVisRDFModel(null);
            modelHerder.reloadData();
            return "Profile changed successfully.";
        }
        else {
            return "Profile not changed.";
        }
    }

}
