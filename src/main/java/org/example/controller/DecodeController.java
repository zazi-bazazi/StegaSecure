package org.example.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.stego.Engine;

import java.io.*;

/**
 * Controller for the Decode screen.
 * Handles stego-image and key-file upload, decoding via Engine,
 * and displaying/downloading the recovered message.
 */
public class DecodeController {

    // --- FXML fields ---
    @FXML private Button btnUploadImage;
    @FXML private Label lblImageName;
    @FXML private Label lblImageWarning;

    @FXML private Button btnUploadKey;
    @FXML private Label lblKeyName;

    @FXML private Button btnStartDecode;
    @FXML private ProgressIndicator progressDecode;
    @FXML private Label lblDecodeStatus;

    @FXML private VBox resultBox;
    @FXML private Label lblSuccess;
    @FXML private TextArea txtDecodedMessage;
    @FXML private Button btnDownloadText;

    // --- State ---
    private File selectedStegoImage;
    private File selectedKeyFile;
    private String decodedText;

    // -------------------------------------------------------------------------
    // Upload stego image
    // -------------------------------------------------------------------------
    @FXML
    private void onUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Stego Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            selectedStegoImage = file;
            lblImageName.setText(file.getName());

            // Check if non-PNG and show warning
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".png")) {
                lblImageWarning.setText("⚠ Decoding may not work correctly on non-PNG images");
                lblImageWarning.setVisible(true);
                lblImageWarning.setManaged(true);
            } else {
                lblImageWarning.setVisible(false);
                lblImageWarning.setManaged(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Upload key file
    // -------------------------------------------------------------------------
    @FXML
    private void onUploadKeyFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Key File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Key Files", "*.txt", "*.key"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            selectedKeyFile = file;
            lblKeyName.setText(file.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Start decoding
    // -------------------------------------------------------------------------
    @FXML
    private void onStartDecode() {
        // Validate inputs
        if (selectedStegoImage == null) {
            showError("Please upload a stego image first.");
            return;
        }
        if (selectedKeyFile == null) {
            showError("Please upload a key file first.");
            return;
        }

        // Show progress
        lblDecodeStatus.setText("");
        progressDecode.setVisible(true);
        progressDecode.setManaged(true);
        btnStartDecode.setDisable(true);
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        Task<String> decodeTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                Engine engine = Engine.getInstance();
                AbstractChromosome<?> chromosome = engine.loadChromosomeKey(selectedKeyFile);
                return engine.decode(selectedStegoImage, chromosome);
            }
        };

        decodeTask.setOnSucceeded(event -> {
            decodedText = decodeTask.getValue();

            // Check for decode errors from Engine
            if (decodedText != null && decodedText.startsWith("[DECODE ERROR]")) {
                showError(decodedText);
                progressDecode.setVisible(false);
                progressDecode.setManaged(false);
                btnStartDecode.setDisable(false);
                return;
            }

            // Show success result
            txtDecodedMessage.setText(decodedText);
            lblDecodeStatus.setText("");
            resultBox.setVisible(true);
            resultBox.setManaged(true);

            progressDecode.setVisible(false);
            progressDecode.setManaged(false);
            btnStartDecode.setDisable(false);
        });

        decodeTask.setOnFailed(event -> {
            Throwable ex = decodeTask.getException();
            showError("Decoding failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
            progressDecode.setVisible(false);
            progressDecode.setManaged(false);
            btnStartDecode.setDisable(false);
        });

        new Thread(decodeTask, "decode-thread").start();
    }

    // -------------------------------------------------------------------------
    // Download decoded text as .txt
    // -------------------------------------------------------------------------
    @FXML
    private void onDownloadText() {
        if (decodedText == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Decoded Message");
        chooser.setInitialFileName("decoded_message.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = chooser.showSaveDialog(getStage());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(decodedText);
            } catch (IOException e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Back to main menu
    // -------------------------------------------------------------------------
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/view/MainMenuView.fxml"));
            Parent root = loader.load();

            Stage stage = getStage();
            Scene scene = new Scene(root, 850, 520);
            scene.getStylesheets().add(
                    getClass().getResource("/org/example/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("StegaSecure");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private Stage getStage() {
        return (Stage) btnUploadImage.getScene().getWindow();
    }

    private void showError(String message) {
        lblDecodeStatus.setText(message);
        lblDecodeStatus.setStyle("-fx-text-fill: #e74c3c;");
    }
}
