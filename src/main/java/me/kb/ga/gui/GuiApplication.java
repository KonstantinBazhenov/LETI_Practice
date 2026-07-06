package me.kb.ga.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GuiApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = getClass().getResource("/main-view.fxml");

        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find main-view.fxml in resources");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 900, 700);

        stage.setTitle("Sudoku Genetic Algorithm");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}