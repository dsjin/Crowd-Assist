package th.ac.kmitl.it.crowdalert.model;

import java.io.Serializable;

public class AssistantModel implements Serializable{
    private String assistantUid;
    private Double lat;
    private Double lng;
    private String role;

    public String getAssistantUid() {
        return assistantUid;
    }

    public void setAssistantUid(String assistantUid) {
        this.assistantUid = assistantUid;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
