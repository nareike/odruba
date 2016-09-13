package odruba.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.BufferedReader;
import java.io.StringReader;

public class RulesetData {

    private String name;
    private String data;

    public void setName(String name) {
        this.name = name;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    @JsonIgnore
    public BufferedReader getDataStream() {
        return new BufferedReader(new StringReader(data));
    }

}
