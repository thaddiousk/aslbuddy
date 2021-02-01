package example.org.aslbuddy;

import java.io.Serializable;

public class ASLObject implements Serializable {
    private String name;
    private int drawable;
    private String fileType;
    private boolean learned;

    public ASLObject(String name, int drawable, String fileType, boolean learned) {
        this.name = name;
        this.drawable = drawable;
        this.fileType = fileType;
        this.learned = learned;
    }

    public String getName() {
        return name;
    }

    public int getDrawableNum() {
        return drawable;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean getLearned() {
        return learned;
    }

    public void useHasLearned() {
        learned = true;
    }
}