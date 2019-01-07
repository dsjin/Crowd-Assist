package th.ac.kmitl.it.crowdalert.model;

import java.io.Serializable;

public class EmergencyRequestModel extends Request implements Serializable{
    private String area;
    private Integer time;

    public EmergencyRequestModel() {
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }


}
