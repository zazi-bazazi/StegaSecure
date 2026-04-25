package org.example.controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.model.stego.Engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

/**
 * Controller for the Encode screen.
 * Handles cover-image upload, secret text input (typed or from .txt file),
 * encoding via Engine, and result display with download options.
 */
public class EncodeController {

    // --- FXML fields ---
    @FXML private Button btnUploadImage;
    @FXML private Label lblImageName;
    @FXML private ImageView coverImageView;

    @FXML private RadioButton radioText;
    @FXML private RadioButton radioFile;
    @FXML private ToggleGroup sourceToggle;

    @FXML private TextArea txtSecretText;
    @FXML private HBox fileUploadBox;
    @FXML private Button btnUploadTextFile;
    @FXML private Label lblTextFileName;

    @FXML private Button btnStartEncode;
    @FXML private ProgressIndicator progressEncode;
    @FXML private Label lblEncodeStatus;

    @FXML private VBox resultBox;
    @FXML private ImageView stegoImageView;
    @FXML private Label lblEncodeSuccess;
    @FXML private Button btnDownloadKey;

    // --- State ---
    private File selectedCoverImage;
    private File selectedTextFile;
    private BufferedImage stegoResultImage;
    private String keyFileData;

    // -------------------------------------------------------------------------
    // Upload cover image
    // -------------------------------------------------------------------------
    @FXML
    private void onUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Cover Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            selectedCoverImage = file;
            lblImageName.setText(file.getName());

            // Show cover image preview
            try {
                Image preview = new Image(file.toURI().toString());
                coverImageView.setImage(preview);
            } catch (Exception e) {
                // Silently ignore preview errors
            }
        }
    }

    // -------------------------------------------------------------------------
    // Toggle between text input and file input
    // -------------------------------------------------------------------------
    @FXML
    private void onToggleSource() {
        boolean useFile = radioFile.isSelected();
        txtSecretText.setVisible(!useFile);
        txtSecretText.setManaged(!useFile);
        fileUploadBox.setVisible(useFile);
        fileUploadBox.setManaged(useFile);
    }

    // -------------------------------------------------------------------------
    // Upload .txt secret file
    // -------------------------------------------------------------------------
    @FXML
    private void onUploadTextFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Text File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            selectedTextFile = file;
            lblTextFileName.setText(file.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Start encoding
    // -------------------------------------------------------------------------
    @FXML
    private void onStartEncode() {
        // Validate inputs
        if (selectedCoverImage == null) {
            showError("Please upload a cover image first.");
            return;
        }

        String secretText;
        if (radioText.isSelected()) {
            secretText = txtSecretText.getText();
            if (secretText == null || secretText.isBlank()) {
                showError("Please enter a secret message.");
                return;
            }
        } else {
            if (selectedTextFile == null) {
                showError("Please upload a .txt file.");
                return;
            }
            try {
                secretText = Files.readString(selectedTextFile.toPath());
            } catch (IOException e) {
                showError("Failed to read text file: " + e.getMessage());
                return;
            }
        }

        // Show progress, disable buttons
        lblEncodeStatus.setText("");
        lblEncodeSuccess.setText("");
        progressEncode.setVisible(true);
        progressEncode.setManaged(true);
        btnStartEncode.setDisable(true);
        btnDownloadKey.setDisable(true);

        final String finalSecret = secretText;

        Task<Engine.FilesRecord> encodeTask = new Task<>() {
            @Override
            protected Engine.FilesRecord call() throws Exception {
                Engine engine = Engine.getInstance();
                return engine.encode(finalSecret, selectedCoverImage);
            }
        };

        encodeTask.setOnSucceeded(event -> {
            Engine.FilesRecord result = encodeTask.getValue();
            stegoResultImage = result.stegoImage();
            keyFileData = result.keyFileInString();

            // Update result area
            stegoImageView.setImage(SwingFXUtils.toFXImage(stegoResultImage, null));
            lblEncodeSuccess.setText("✓ Encoding Complete!");
            btnDownloadKey.setDisable(false);

            progressEncode.setVisible(false);
            progressEncode.setManaged(false);
            btnStartEncode.setDisable(false);
        });

        encodeTask.setOnFailed(event -> {
            Throwable ex = encodeTask.getException();
            showError("Encoding failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
            progressEncode.setVisible(false);
            progressEncode.setManaged(false);
            btnStartEncode.setDisable(false);
        });

        new Thread(encodeTask, "encode-thread").start();
    }

    // -------------------------------------------------------------------------
    // Click stego image → save
    // -------------------------------------------------------------------------
    @FXML
    private void onImageClicked() {
        if (stegoResultImage == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Stego Image");
        chooser.setInitialFileName("stego_result.png");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = chooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                ImageIO.write(stegoResultImage, "png", file);
            } catch (IOException e) {
                showError("Failed to save image: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Download key file
    // -------------------------------------------------------------------------
    @FXML
    private void onDownloadKey() {
        if (keyFileData == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Key File");
        chooser.setInitialFileName("stego_key.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = chooser.showSaveDialog(getStage());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(keyFileData);
            } catch (IOException e) {
                showError("Failed to save key file: " + e.getMessage());
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
            Scene scene = new Scene(root, 750, 520);
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
        lblEncodeStatus.setText(message);
        lblEncodeStatus.setStyle("-fx-text-fill: #e74c3c;");
    }
}
