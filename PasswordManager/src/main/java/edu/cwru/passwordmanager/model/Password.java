package edu.cwru.passwordmanager.model;

public class Password {
    public static enum Tag {
        VALID,
        INVALID,
        EMPTY
    }
    public final Tag tag;
    private String label;
    private String password;

    public Password(String label, String password, Tag tag) {
        this.tag = tag;
        this.label = label;
        this.password = password;
    }

    public Password(String label, String password) {
        tag = Tag.VALID;
        this.label = label;
        this.password = password;
    }

    @Override
    public String toString() {
        return this.label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Password invalidPassword() {
        return new Password("Invalid Password", "Line Malformed", Tag.INVALID);
    }

    public static Password emptyPassword() {
        return new Password("Empty Line", "", Tag.EMPTY);
    }
}
