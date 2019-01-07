package th.ac.kmitl.it.crowdalert.model;

import java.util.List;

public class Item {
    public int type;
    public String text;
    public List<Item> invisibleChildren;
    public String uid;
    public Long timestamp;

    public Item() {
    }

    public Item(int type, String text, String uid, Long timestamp) {
        this.type = type;
        this.text = text;
        this.uid = uid;
        this.timestamp = timestamp;
    }
    public Item(int type, String text, List<Item> invisibleChildren) {
        this.type = type;
        this.text = text;
        this.invisibleChildren = invisibleChildren;
    }
}
