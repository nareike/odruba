package odruba.pojo.vis;

import java.io.Serializable;

public class Icon implements Serializable {

    private String face;
    private String code;
    private Integer size;
    private String color;

    public void setFace(String face) {
        this.face = face;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // --- G E T T E R S ---

    public String getFace() {
        return face;
    }

    public String getCode() {
        return code;
    }

    public Integer getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }
}
