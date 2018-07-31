package th.ac.kmitl.it.crowdassist.modal;

import java.io.Serializable;

public class GeneralRequestModel extends Request implements Serializable {
    private String type;
    private String description;
    private String title;

    public GeneralRequestModel() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
