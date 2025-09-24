package edu.cwru.passwordmanager.model;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private SecretKeySpec key;

    public AES(byte[] AESkey) {
        key = new SecretKeySpec(AESkey, "AES");
    }

    public byte[] encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return encrypted;
    }

    public String decrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted);
    }

    public String encryptString(String data) throws Exception {
        return Tools.encode(encrypt(data));
    }

    public String decryptString(String encryptedString) throws Exception {
        return decrypt(Tools.decode(encryptedString));
    }
}
