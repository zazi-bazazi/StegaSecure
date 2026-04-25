package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application entry point.
 * Loads the main menu view and applies the stylesheet.
 */
public class Main extends Application {

    private static final double WINDOW_WIDTH = 850;
    private static final double WINDOW_HEIGHT = 520;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/view/MainMenuView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/org/example/view/styles.css").toExternalForm());

        primaryStage.setTitle("StegaSecure");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}