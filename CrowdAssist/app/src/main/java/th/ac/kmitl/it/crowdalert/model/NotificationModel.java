package th.ac.kmitl.it.crowdalert.model;

public class NotificationModel {
    private String uid;
    private String username;
    private Long timestamp;
    private Double distance;
    private UserModel user;
    private String type;
    private String requesterUid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public NotificationModel(String uid, String username, Long timestamp, Double distance, UserModel user, String type, String requesterUid) {
        this.uid = uid;
        this.username = username;
        this.timestamp = timestamp;
        this.distance = distance;
        this.user = user;
        this.type = type;
        this.requesterUid = requesterUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequesterUid() {
        return requesterUid;
    }

    public void setRequesterUid(String requesterUid) {
        this.requesterUid = requesterUid;
    }
}
