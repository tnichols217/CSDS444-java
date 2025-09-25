package edu.cwru.passwordmanager.model;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class Argon {
    public static class Params {
        public static final int version = 19;
        public int memory;
        public int iterations;
        public int parallelism;

        public Params(int m, int t, int p){
            memory = m;
            iterations = t;
            parallelism = p;
        }
    }

    private static class HashParts {
        public Params p;
        public byte[] salt;
        public byte[] hash;

        public HashParts(Params params, byte[] s, byte[] h) {
            p = params;
            salt = s;
            hash = h;
        }
    }

    private Params p;

    public Argon(Params argonParams) {
        p = argonParams;
    }

    public byte[] getArgonBytes(String password, byte[] salt, boolean isCheck) {
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withParallelism(1)
            .withMemoryAsKB(p.memory)
            .withIterations(isCheck ? p.iterations * 2 : p.iterations);

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] key = new byte[32];
        generator.generateBytes(password.toCharArray(), key, 0, key.length);

        return key;
    }

    public byte[] getArgonBytes(String password, byte[] salt) {
        return getArgonBytes(password, salt, false);
    }

    public String makeArgonString(byte[] salt, byte[] hash) {
        return String.format(
            "$argon2id$v=%d$m=%d,t=%d,p=%d$%s$%s",
            Params.version, p.memory, p.iterations * 2, p.parallelism,
            Tools.encode(salt, true), Tools.encode(hash, true)
        );
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    private static HashParts getHashParts(String argon) {
        try {
            String[] parts = argon.split("\\$");
            if (parts.length != 6) return null;

            String[] params = parts[3].split(",");
            int m1 = Integer.parseInt(params[0].split("=")[1]);
            int t1 = Integer.parseInt(params[1].split("=")[1]);
            int p1 = Integer.parseInt(params[2].split("=")[1]);

            byte[] salt = Tools.decode(parts[4]);
            byte[] expectedHash = Tools.decode(parts[5]);
            return new HashParts(new Params(m1, t1, p1), salt, expectedHash);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkArgonString(String argon, String password) {
        try {
            HashParts hp = getHashParts(argon);
            if (hp == null) return false;

            hp.p.iterations = hp.p.iterations / 2;

            Argon argon_hasher = new Argon(hp.p);

            byte[] computedHash = argon_hasher.getArgonBytes(password, hp.salt, true);

            return constantTimeEquals(hp.hash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] getSaltFromArgonString(String argon) {
        HashParts hp = getHashParts(argon);
        if (hp == null) return null;
        return hp.salt;
    }
}
