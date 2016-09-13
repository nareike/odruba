package odruba.fontawesome;

import java.util.List;

public class IconDefinition {
    private String name;
    private String id;
    private String unicode;
    private String created;
    private List<String> filter;
    private List<String> categories;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUnicode() {
        return unicode;
    }

    public String getCreated() {
        return created;
    }

    public List<String> getFilter() {
        return filter;
    }

    public List<String> getCategories() {
        return categories;
    }
}
