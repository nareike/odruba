package odruba;

import java.util.Arrays;
import java.util.List;

public enum DashType {

    DASHED(Arrays.asList(10,8)),
    DOTDASHED(Arrays.asList(6,6,1,6)),
    DOTTED(Arrays.asList(1,6));

    private final List dashList;

    DashType(List<Integer> dashList) {
        this.dashList = dashList;
    }

    public List<Integer> asList() {
        return dashList;
    }
}
