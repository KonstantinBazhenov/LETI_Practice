package me.kb.ga.gui;

import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.kb.ga.data.GAConfig;
import me.kb.ga.main.GASudokuSession;
import me.kb.ga.sudoku.SudokuType;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class GuiApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = getClass().getResource("/main-view.fxml");

        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find main-view.fxml in resources");
        }


        File configFile = new File("ga-config.json");
        GAConfig config = GAConfig.builder().build();


        if (configFile.exists()) {
            try {
                config = GAConfig.read(configFile);
            } catch (Exception e) {
                System.out.println("Failed to load config: " + e.getMessage());
                e.printStackTrace();
            }
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        MainGuiController controller = loader.getController();

        GASudokuSession session = new GASudokuSession(config, SudokuType.SUDOKU_9);

        controller.setSession(session);

        Scene scene = new Scene(root, 900, 700);

        stage.setTitle("Sudoku Genetic Algorithm");
        stage.setScene(scene);
        stage.show();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                session.getGeneticAlgorithm().getConfig().write(configFile);
            } catch (Exception e) {
                System.out.println("Failed to save config: " + e.getMessage());
                e.printStackTrace();
            }
        }));
    }
}