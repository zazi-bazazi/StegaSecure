package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

/**
 * Controller for the main menu screen.
 * Provides navigation to Encode and Decode views.
 */
public class MainMenuController {

    @FXML private Button btnEncode;
    @FXML private Button btnDecode;

    @FXML
    private void onEncodeClicked() {
        navigateTo("/org/example/view/EncodeView.fxml", "StegaSecure — Encode");
    }

    @FXML
    private void onDecodeClicked() {
        navigateTo("/org/example/view/DecodeView.fxml", "StegaSecure — Decode");
    }

    /**
     * Loads the given FXML into the current stage.
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnEncode.getScene().getWindow();
            Scene scene = new Scene(root, 850, 520);
            scene.getStylesheets().add(
                    getClass().getResource("/org/example/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
