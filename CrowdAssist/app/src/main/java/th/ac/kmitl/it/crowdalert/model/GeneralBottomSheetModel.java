package th.ac.kmitl.it.crowdalert.model;

public class GeneralBottomSheetModel {
    private String name;
    private Integer icon;

    public GeneralBottomSheetModel(String name, Integer icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
