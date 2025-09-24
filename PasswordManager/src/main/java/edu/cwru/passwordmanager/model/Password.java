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
    private TOTP totp;
    private String totpSecret;

    public Password(String label, String password, Tag tag) {
        this.tag = tag;
        this.label = label;
        this.password = password;
        this.totpSecret = "";
    }

    public Password(String label, String password) {
        tag = Tag.VALID;
        this.label = label;
        this.password = password;
        this.totpSecret = "";
    }

    public Password(String label, String password, String TotpSecret) {
        tag = Tag.VALID;
        this.label = label;
        this.password = password;
        this.totpSecret = TotpSecret;
        this.totp = new TOTP(TotpSecret);
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

    public String getTOTP() {
        try {
            return totp.getCode();
        } catch (Exception e) {
            return "";
        }
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTOTP(String TotpSecret) {
        this.totpSecret = TotpSecret;
        this.totp = new TOTP(TotpSecret);
    }

    public static Password invalidPassword() {
        return new Password("Invalid Password", "Line Malformed", Tag.INVALID);
    }

    public static Password emptyPassword() {
        return new Password("Empty Line", "", Tag.EMPTY);
    }
}
