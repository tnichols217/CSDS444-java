package edu.cwru.passwordmanager.model;

import io.github.kamilszewc.totp.Totp;

public class TOTP {
    private String secret;
    
    public TOTP(String secret) {
        this.secret = secret;
    }

    public String getCode() throws Exception {
        return Totp.getCode(secret);
    }
}
