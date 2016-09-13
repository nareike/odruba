package odruba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 Just a convenience function to read rulesets from application.yaml
 */
@Component
@ConfigurationProperties(prefix="odruba.data")
public class RulesetList {

    private List<String> rulesets = new ArrayList<String>();

    public List<String> getRulesets() {
        return rulesets;
    }

    public void setRulesets(List<String> rulesets) {
        this.rulesets = rulesets;
    }

}
