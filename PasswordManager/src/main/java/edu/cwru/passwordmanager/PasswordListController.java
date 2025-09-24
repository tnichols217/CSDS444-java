package edu.cwru.passwordmanager;

import edu.cwru.passwordmanager.model.Password;
import edu.cwru.passwordmanager.model.PasswordModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
// import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
// import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasswordListController implements Initializable {
    private PasswordModel pm;

    private FilteredList<Password> filteredList;

    @FXML private ListView<Password> passwordListView;

    @FXML private TextField passwordLabel;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField TotpSecretField;
    @FXML private TextField TotpField;

    @FXML private Button deleteButton;
    @FXML private Button saveButton;
    @FXML private Button copyTotpButton;
    @FXML private Button copyTotpSecretButton;

    private Timeline totpUpdater;

    public String passcode = null;

    public PasswordListController() {
    }

    public void setModel(PasswordModel passwordModel) {
        pm = passwordModel;

        filteredList = new FilteredList<>(
            pm.getPasswords(), p -> p.tag != Password.Tag.EMPTY
        );

        passwordListView.setItems(filteredList);

        passwordListView.setOnMouseClicked(mouseEvent -> {
            loadPasswordDetail();
        });
        passwordListView.setOnKeyPressed(keyEvent -> {
            loadPasswordDetail();
        });

       disableFields();
    }

    private void enableFields() {
        passwordLabel.setDisable(false);
        passwordField.setDisable(false);
        TotpSecretField.setDisable(false);
        deleteButton.setDisable(false);
        saveButton.setDisable(false);
        copyTotpButton.setDisable(false);
        copyTotpSecretButton.setDisable(false);
    }
    private void disableFields() {
        passwordLabel.setDisable(true);
        passwordField.setDisable(true);
        TotpSecretField.setDisable(true);
        deleteButton.setDisable(true);
        saveButton.setDisable(true);
        copyTotpButton.setDisable(true);
        copyTotpSecretButton.setDisable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TotpField.setDisable(true);
    }

    private void clearPasswordDetail() {
        passwordLabel.setText("");
        passwordField.setText("");
        TotpField.setText("");
        TotpSecretField.setText("");
        totpUpdater.stop();
        totpUpdater = null;
        TotpField.setText("");

        disableFields();
    }

    private void loadPasswordDetail() {
        // Show the detail of the password
        int index = passwordListView.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Password selectedPassword = filteredList.get(index);

        passwordLabel.setText(selectedPassword.getLabel());
        passwordField.setText(selectedPassword.getPassword());
        TotpSecretField.setText(selectedPassword.getTotpSecret());

        totpUpdater = new Timeline(
            new KeyFrame(Duration.ZERO, e -> updateTotp()),
            new KeyFrame(Duration.seconds(1))
        );
        totpUpdater.setCycleCount(Timeline.INDEFINITE);
        totpUpdater.play();

        enableFields();
    }

    @FXML
    protected void copyPassword() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(passwordField.getText());
        clipboard.setContent(content);
    }

    @FXML
    protected void deleteButtonClicked() throws Exception {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete this password?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> selected = alert.showAndWait();
        if (selected.isPresent() && selected.get().equals(ButtonType.YES)) {
            pm.deletePassword(filteredList.getSourceIndex(passwordListView.getSelectionModel().getSelectedIndex()));
            clearPasswordDetail();
        }
    }

    @FXML
    protected void saveButtonClicked() throws Exception {
        int index = passwordListView.getSelectionModel().getSelectedIndex();

        String label = passwordLabel.getText();
        String password = passwordField.getText();
        String totpSecret = TotpSecretField.getText();

        pm.updatePassword(new Password(label, password, totpSecret), filteredList.getSourceIndex(index));
        passwordListView.getSelectionModel().select(index);
    }

    @FXML
    protected void addPassword() {
        // Create new password and select empty space, then load detail
        int index = pm.addPassword(new Password("New Password", ""));
        passwordListView.getSelectionModel().select(index == -1 ? filteredList.size() - 1 : filteredList.getSourceIndex(index));
        loadPasswordDetail();
    }

    @FXML
    protected void copyTotp() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        int index = passwordListView.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Password selectedPassword = filteredList.get(index);
        content.putString(selectedPassword.getTOTP());
        clipboard.setContent(content);
    }

    @FXML
    protected void copyTotpSecret() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        int index = passwordListView.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Password selectedPassword = filteredList.get(index);
        content.putString(selectedPassword.getTotpSecret());
        clipboard.setContent(content);
    }

    private void updateTotp() {
        int index = passwordListView.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Password selectedPassword = filteredList.get(index);
        String totpCode = selectedPassword.getTOTP();
        TotpField.setText(totpCode);
    }
}
