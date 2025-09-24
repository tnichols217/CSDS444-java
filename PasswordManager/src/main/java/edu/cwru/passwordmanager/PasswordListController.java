package edu.cwru.passwordmanager;

import edu.cwru.passwordmanager.model.Password;
import edu.cwru.passwordmanager.model.PasswordModel;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasswordListController implements Initializable {
    private PasswordModel pm;

    private FilteredList<Password> filteredList;

    @FXML private ListView<Password> passwordListView;

    @FXML private TextField passwordLabel;

    @FXML private PasswordField passwordField;

    @FXML private Button deleteButton;
    @FXML private Button saveButton;

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
        deleteButton.setDisable(false);
        saveButton.setDisable(false);
    }
    private void disableFields() {
        passwordLabel.setDisable(true);
        passwordField.setDisable(true);
        deleteButton.setDisable(true);
        saveButton.setDisable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    private void clearPasswordDetail() {
        passwordLabel.setText("");
        passwordField.setText("");

        disableFields();
    }

    private void loadPasswordDetail() {
        // Show the detail of the password
        int index = passwordListView.getSelectionModel().getSelectedIndex();

        Password selectedPassword =filteredList.get(index);

        passwordLabel.setText(selectedPassword.getLabel());
        passwordField.setText(selectedPassword.getPassword());

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
        String label = passwordLabel.getText();
        String password = passwordField.getText();

        int selectedIndex = passwordListView.getSelectionModel().getSelectedIndex();

        pm.updatePassword(new Password(label, password), filteredList.getSourceIndex(selectedIndex));
    }

    @FXML
    protected void addPassword() {
        // Create new password and select last one, then load detail
        int index = pm.addPassword(new Password("New Password", ""));
        passwordListView.getSelectionModel().select(index == -1 ? filteredList.size() - 1 : filteredList.getSourceIndex(index));
        loadPasswordDetail();
    }
}
