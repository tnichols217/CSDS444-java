package edu.cwru.passwordmanager.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class PasswordModel {
    private ObservableList<Password> passwords = FXCollections.observableArrayList();

    // !!! DO NOT CHANGE - VERY IMPORTANT FOR GRADING !!!
    private String passwordFile;
    private String separator = "\t";

    // I don't know why you want to store these but its apparently necessary for grading?
    private String passwordFilePassword = "";
    private String argonHash;
    private byte [] passwordFileKey;

    public static final int ARGON_ITER = 5; // OWASP recommends at least 2
    public static final int ARGON_MEM = 65536; // OWASP recommends at least 16MB
    public static final int LINE_SIZE = 512; // Size of one line in bytes

    private Argon argon;
    private AES aes;

    private boolean loadPasswords(String password) throws IOException{
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(passwordFile))) {
            // Read and parse the first line (Argon2 hash)
            String argonHash = reader.readLine().trim();
            if (argonHash == null || argonHash.isEmpty()) {
                throw new IOException("Password file is empty or corrupted (no Argon hash).");
            }

            this.argonHash = argonHash;

            if (!Argon.checkArgonString(argonHash, password)) return false;

            byte[] salt = Argon.getSaltFromArgonString(argonHash);
            byte[] argonBytes = argon.getArgonBytes(password, salt);
            passwordFileKey = argonBytes;
            aes = new AES(argonBytes);


            // Stream remaining lines (password entries)
            reader.lines()
                .filter(line -> !line.trim().isEmpty())
                .forEach(line -> {
                    try {
                        String[] parts = line.split(separator);
                        if (parts.length != 2) return;  // Skip broken lines

                        String label = parts[0].trim();
                        String encryptedPassword = parts[1];
                        String rawPassword = encryptedPassword.trim();
                        if (rawPassword.compareTo("") == 0) {
                            passwords.add(Password.emptyPassword());
                        }
                        String decryptedPassword = aes.decryptString(rawPassword);

                        passwords.add(new Password(label, decryptedPassword));
                    } catch (Exception e) {
                        System.err.println("Failed to decrypt a password line: " + e.getMessage());
                        passwords.add(Password.invalidPassword());
                    }
                });
            return true;
        }
    }

    private String generatePasswordLine(Password password) throws Exception {
        if (password.tag == Password.Tag.EMPTY) return "";
        String line = String.format("%s%s%s", password.getLabel(), separator, aes.encryptString(password.getPassword()));
        return line;
    }

    private boolean writePasswordLine(int index) {
        try {
            String line = (index == -1) ?
                generatePasswordLine(passwords.get(passwords.size()-1)) :
                generatePasswordLine(passwords.get(index));
            Tools.writeFile(passwordFile, line, (index == -1) ? -1 : index + 1, LINE_SIZE);
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
        return true;
    }

    public PasswordModel(String fp) {
        passwordFile = fp;
        argon = new Argon(new Argon.Params(ARGON_MEM, ARGON_ITER, 1));
    }

    public boolean passwordFileExists() {
        return new File(passwordFile).exists();
    }

    public void initializePasswordFile(String password) throws IOException {
        File f = new File(passwordFile);
        f.createNewFile();
        passwordFilePassword = password;
        byte[] passwordFileSalt = Tools.generateSalt();
        passwordFileKey = argon.getArgonBytes(password, passwordFileSalt);

        byte[] passwordHash = argon.getArgonBytes(password, passwordFileSalt, true);
        String argonHash = argon.makeArgonString(passwordFileSalt, passwordHash);

        Tools.writeFile(passwordFile, argonHash, 0, LINE_SIZE);
        loadPasswords(password);
    }

    public boolean verifyPassword(String password) {
        passwordFilePassword = password; // DO NOT CHANGE
        try {
            return loadPasswords(password);
        } catch (Exception e) {
            return false;
        }
    }

    public ObservableList<Password> getPasswords() {
        return passwords;
    }

    public void deletePassword(int index) throws Exception {
        passwords.set(index, Password.emptyPassword());
        boolean status = writePasswordLine(index);
        if (!status) throw new Exception("Could not delete password");
    }

    public void updatePassword(Password password, int index) throws Exception {
        passwords.set(index, password);
        boolean status = writePasswordLine(index);
        if (!status) throw new Exception("Could not write password");
    }

    public int addPassword(Password password) {
        int emptyIndexOpt = IntStream.range(0, passwords.size())
            .filter(i -> passwords.get(i).tag == Password.Tag.EMPTY)
            .findFirst()
            .orElse(-1);

        if (emptyIndexOpt == -1) {
            passwords.add(password);
        } else {
            passwords.set(emptyIndexOpt, password);
        }
        writePasswordLine(emptyIndexOpt);
        return emptyIndexOpt;
    }
}
