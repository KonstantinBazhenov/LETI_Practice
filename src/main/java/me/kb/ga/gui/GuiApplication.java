package me.kb.ga.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.kb.ga.data.GAConfig;
import me.kb.ga.main.GASudokuSession;
import me.kb.ga.sudoku.SudokuType;

import java.io.IOException;
import java.net.URL;

public class GuiApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = getClass().getResource("/main-view.fxml");

        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find main-view.fxml in resources");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        MainGuiController controller = loader.getController();
        controller.setSession(new GASudokuSession(
                GAConfig.builder()
                        .iterationsPerRun(1500)
                        .populationSize(500)
                        .mutationRate(0.09)
                        .crossoverRate(0.75)
                        .copyBestRate(0.02)
                        .similarityThreshold(0.82)
                        .similarityPunishment(2.5)
                        .stagnationKeepRate(0.05)
                        .stagnationGenerations(100)
                        .similaritySkip(3)
                        .similarityCompare(30).build(),
                SudokuType.SUDOKU_9
        ));

        Scene scene = new Scene(root, 900, 700);

        stage.setTitle("Sudoku Genetic Algorithm");
        stage.setScene(scene);
        stage.show();
    }
}