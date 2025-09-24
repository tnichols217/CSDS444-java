package edu.cwru.passwordmanager;

import edu.cwru.passwordmanager.model.PasswordModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InitialController implements Initializable {
    @FXML private Label passwordLabel;

    @FXML private PasswordField passwordField;

    @FXML private Label error;

    private PasswordModel pm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pm = new PasswordModel("passwords.txt");
        if (! pm.passwordFileExists()) {
            // Update label as this is a new file
            passwordLabel.setText("Please enter a password to protect your passwords.");
        }
    }

    @FXML
    protected void passwordButtonClicked() throws IOException {
        String passwordFilePassword = passwordField.getText();
        if (! pm.passwordFileExists()) {
            pm.initializePasswordFile(passwordFilePassword);
            navigateToList();
        }
        else {
            // If user enters correct password, go to list
            if ( pm.verifyPassword(passwordFilePassword)) {
                navigateToList();
            }
            else {
                error.setVisible(true);
            }
        }
    }

    private void navigateToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("passwordlist-view.fxml"));
            
            Parent root = loader.load();

            PasswordListController controller = loader.getController();
            controller.setModel(pm);

            PasswordApplication.primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}