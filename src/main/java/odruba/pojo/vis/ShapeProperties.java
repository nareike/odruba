package odruba.pojo.vis;

import java.io.Serializable;

public class ShapeProperties implements Serializable {

    private Integer[] borderDashes;
    private Integer borderRadius;

    private Boolean interpolation;
    private Boolean useImageSize;
    private Boolean useBorderWithImage;

    public void setBorderDashes(Integer... borderDashes) {
        this.borderDashes = borderDashes;
    }

    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
    }

    public void setInterpolation(Boolean interpolation) {
        this.interpolation = interpolation;
    }

    public void setUseImageSize(Boolean useImageSize) {
        this.useImageSize = useImageSize;
    }

    public void setUseBorderWithImage(Boolean useBorderWithImage) {
        this.useBorderWithImage = useBorderWithImage;
    }

    // --- G E T T E R S ---

    public Integer[] getBorderDashes() {
        return borderDashes;
    }

    public Integer getBorderRadius() {
        return borderRadius;
    }

    public Boolean getInterpolation() {
        return interpolation;
    }

    public Boolean getUseImageSize() {
        return useImageSize;
    }

    public Boolean getUseBorderWithImage() {
        return useBorderWithImage;
    }
}
