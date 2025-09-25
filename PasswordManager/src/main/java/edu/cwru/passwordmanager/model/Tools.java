package edu.cwru.passwordmanager.model;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class Tools {
    public static byte[] generateSalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static String encode(byte[] bytes, boolean noPad) {
        String encoded = Base64.getEncoder().encodeToString(bytes);
        if (noPad) {
            encoded = encoded.replace("=", "");
        }
        return encoded;
    }

    public static String encode(byte[] bytes) {
        return encode(bytes, false);
    }

    public static byte[] decode(String string) {
        int paddingNeeded = (4 - (string.length() % 4)) % 4;
        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < paddingNeeded; i++) {
            sb.append('=');
        }
        return Base64.getDecoder().decode(sb.toString());
    }

    public static byte[] padString(String string, int size) {
        byte[] lineBytes = string.getBytes(StandardCharsets.UTF_8);
        byte[] padded = new byte[size];

        Arrays.fill(padded, (byte) ' ');
        System.arraycopy(lineBytes, 0, padded, 0, lineBytes.length);
        padded[size - 1] = (byte) '\n';

        return padded;
    }

    public static boolean writeFile(String fp, String content, int line, int size) {
        try {
            RandomAccessFile fd = new RandomAccessFile(fp, "rw");
            if (line == -1) {
                fd.seek(fd.length());
            } else if (line < -1) {
                fd.close();
                return false;
            }
            fd.seek(line * size);
            fd.write(padString(content, size));
            fd.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
