package odruba.fontawesome;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(locations="classpath:fa-icons.yml", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class FontAwesomeDictionary {

    public List<IconDefinition> icons;

    public void setIcons(List<IconDefinition> icons) {
        this.icons = icons;
    }

    public List<IconDefinition> getIcons() {
        return icons;
    }

    public String getUnicodeByID(String id) {
        Optional<IconDefinition> icon = icons.stream().filter(i-> i.getId().equals(id)).findFirst();
        if (icon.isPresent()) {
            return icon.get().getUnicode();
        }
        else {
            return "";
        }
    }

}
