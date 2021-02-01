package example.org.aslbuddy;

import java.io.Serializable;

public class Profile implements Serializable {
    private int level;
    private int part;

    public Profile(int level, int part) {
        this.level = level;
        this.part = part;
    }

    public int getLevel() {
        return level;
    }

    public int getPart() {
        return part;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPart(int part) {
        this.part = part;
    }
}
