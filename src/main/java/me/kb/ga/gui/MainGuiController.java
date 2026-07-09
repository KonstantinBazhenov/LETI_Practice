package me.kb.ga.gui;

import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.kb.ga.data.DNAScore;
import me.kb.ga.data.GAConfig;
import me.kb.ga.data.RunResult;
import me.kb.ga.data.SudokuCell;
import me.kb.ga.main.GASudokuSession;
import me.kb.ga.sudoku.SudokuBoard;
import me.kb.ga.sudoku.SudokuType;
import me.kb.ga.sudoku.SudokuUtils;
import me.kb.ga.sudoku.matrix.SudokuMatrixView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class MainGuiController {
    private static final JsonMapper jsonMapper = new JsonMapper();

    private static final double GAP = 10.0;
    private final XYChart.Series<Number, Number> graphSeries = new XYChart.Series<>();
    private GASudokuSession session;
    @FXML
    private GridPane root;
    @FXML
    private VBox leftColumn;
    @FXML
    private GridPane rightColumn;
    @FXML
    private StackPane graphContainer;
    @FXML
    private LineChart<Number, Number> graph;
    @FXML
    private NumberAxis graphXAxis;
    @FXML
    private TextArea text;

    @FXML
    private StackPane sudokuContainer;

    @FXML
    private Canvas sudoku;

    @FXML
    private TabPane config;


    @FXML
    private ComboBox<SudokuType> sudokuTypeComboBox;

    @FXML
    private Spinner<Integer> sudokuFilledCellsSpinner;

    @FXML
    private Button sudokuGenerateButton;

    @FXML
    private Button sudokuCreateButton;

    @FXML
    private Button sudokuSaveButton;

    @FXML
    private Button sudokuLoadButton;

    @FXML
    private Button gaRunButton;

    @FXML
    private Button gaStopButton;

    @FXML
    private Spinner<Integer> gaMaxGenerations;

    @FXML
    private Spinner<Integer> gaPopulationSize;

    @FXML
    private Spinner<Double> gaCopyBestRate;

    @FXML
    private Spinner<Double> gaMutationRate;

    @FXML
    private Spinner<Double> gaCrossoverRate;

    @FXML
    private Spinner<Integer> gaGenerationsDelay;

    @FXML
    private Spinner<Integer> gaRandomSeed;

    @FXML
    private Spinner<Integer> gaStagnationGenerations;

    @FXML
    private Spinner<Double> gaStagnationKeepRate;

    @FXML
    private Spinner<Double> gaSimilarityPunishment;

    @FXML
    private Spinner<Integer> gaSimilarityCompare;

    @FXML
    private Spinner<Integer> gaSimilaritySkip;

    @FXML
    private Spinner<Double> gaSimilarityThreshold;

    @FXML
    private Button gaResetConfig;

    @FXML
    private Spinner<Integer> visualizationCurrentGenerationSpinner;

    @FXML
    private Spinner<Integer> visualizationCurrentDnaSpinner;

    @FXML
    private Button visualizationSkipToResultButton;

    private final AtomicBoolean renderQueued = new AtomicBoolean(false);

    @FXML
    private void initialize() {
        bindCanvasSquareToColumn(sudoku, sudokuContainer, leftColumn);

        sudoku.widthProperty().addListener((obs, oldVal, newVal) -> renderSudoku());
        sudoku.heightProperty().addListener((obs, oldVal, newVal) -> renderSudoku());

        graph.getData().add(graphSeries);


        root.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            Runnable updateFont = () -> {
                double scale = Math.min(newScene.getWidth() / 1920, newScene.getHeight() / 1080);

                double fontSize = Math.clamp(scale * 30, 6, 60);

                root.setStyle("-fx-font-size: " + fontSize + "px;");
            };

            newScene.widthProperty().addListener((obs, oldVal, newVal) -> updateFont.run());
            newScene.heightProperty().addListener((obs, oldVal, newVal) -> updateFont.run());

            Platform.runLater(updateFont);
        });

        gaStopButton.setDisable(true);

        leftColumn.setMinWidth(0);
        rightColumn.setMinWidth(0);
        graphContainer.setMinWidth(0);
        sudokuContainer.setMinWidth(0);
        graph.setMinWidth(0);
        config.setMinWidth(0);
        text.setMinWidth(0);

        leftColumn.setMinHeight(0);
        rightColumn.setMinHeight(0);
        graph.setMinHeight(0);
        config.setMinHeight(0);
        text.setMinHeight(0);

    }

    public void setSession(GASudokuSession session) {
        this.session = session;

        Platform.runLater(() -> {
            setupConfig();
            renderGraph();
            renderSudoku();
        });
    }

    @AllArgsConstructor
    private static class ConfigEntry<T> {
        private Spinner<T> spinner;
        private BiConsumer<GAConfig, T> configSetter;
        private Function<GAConfig, T> configGetter;
        Function<T, SpinnerValueFactory<T>> valueFactory;

        public void setSpinnerValue(GAConfig config) {
            spinner.getValueFactory().setValue(configGetter.apply(config));
        }

        public void updateFromSpinnerValue(GAConfig config) {
            T value = spinner.getValue();
            configSetter.accept(config, value);
        }

        public void setup(GAConfig config) {
            spinner.setValueFactory(valueFactory.apply(configGetter.apply(config)));
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
               configSetter.accept(config, newVal);
            });
            spinner.setEditable(true);
        }
    }

    private void setupConfig() {
        int boardSize = session.getBoard().getType().getSize();

        sudokuTypeComboBox.getItems().setAll(SudokuType.values());
        sudokuTypeComboBox.setValue(session.getBoard().getType());

        sudokuTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int maxCells = newVal.getSize() * newVal.getSize();

                int clamped = Math.clamp(sudokuFilledCellsSpinner.getValue(), 0, maxCells);
                sudokuFilledCellsSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxCells, clamped)
                );
            }
        });
        sudokuTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SudokuType type) {
                return type == null ? "" : type.getDisplayName();
            }

            @Override
            public SudokuType fromString(String string) {
                for (SudokuType type : SudokuType.values()) {
                    if (type.getDisplayName().equals(string)) {
                        return type;
                    }
                }
                return null;
            }
        });


        sudokuFilledCellsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, boardSize * boardSize, Math.clamp(45, 0, boardSize * boardSize))
        );
        sudokuFilledCellsSpinner.setEditable(true);

        sudokuGenerateButton.setOnAction(event -> {
            SudokuType selectedType = sudokuTypeComboBox.getValue();
            if (selectedType == null) {
                session.regenerateBoard(sudokuFilledCellsSpinner.getValue());
            } else {
                session.regenerateBoard(selectedType, sudokuFilledCellsSpinner.getValue());
            }
            renderSudoku();
        });

        sudokuCreateButton.setOnAction(event -> {
            try {
                openSudokuCreateWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        sudokuLoadButton.setOnAction(event -> loadSudoku());
        sudokuSaveButton.setOnAction(event -> saveSudoku());


        List<ConfigEntry<?>> gaEntries = List.of(
                new ConfigEntry<>(gaMaxGenerations, GAConfig::setIterationsPerRun, GAConfig::getIterationsPerRun,
                        (v) -> new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000, v)),
                new ConfigEntry<>(gaPopulationSize, GAConfig::setPopulationSize, GAConfig::getPopulationSize,
                        (v) -> new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, v)),
                new ConfigEntry<>(gaCopyBestRate, GAConfig::setCopyBestRate, GAConfig::getCopyBestRate,
                        (v) -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, v, 0.01)),
                new ConfigEntry<>(gaMutationRate, GAConfig::setMutationRate, GAConfig::getMutationRate,
                        (v) -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, v, 0.01)),
                new ConfigEntry<>(gaCrossoverRate, GAConfig::setCrossoverRate, GAConfig::getCrossoverRate,
                        (v) -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, v, 0.01)),
                new ConfigEntry<>(gaGenerationsDelay, GAConfig::setDelayBetweenGenerationsMs, GAConfig::getDelayBetweenGenerationsMs,
                        (v) -> new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, v)),
                new ConfigEntry<>(gaRandomSeed, GAConfig::setRandomSeed, GAConfig::getRandomSeed,
                        (v) -> new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, v)),
                new ConfigEntry<>(gaStagnationGenerations, GAConfig::setStagnationGenerations, GAConfig::getStagnationGenerations,
                        v -> new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100_000, v)),
                new ConfigEntry<>(gaStagnationKeepRate, GAConfig::setStagnationKeepRate, GAConfig::getStagnationKeepRate,
                        v -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, v, 0.01)),
                new ConfigEntry<>(gaSimilarityPunishment, GAConfig::setSimilarityPunishment, GAConfig::getSimilarityPunishment,
                        v -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, v, 0.1)),
                new ConfigEntry<>(gaSimilarityCompare, GAConfig::setSimilarityCompare, GAConfig::getSimilarityCompare,
                        v -> new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, v)),
                new ConfigEntry<>(gaSimilaritySkip, GAConfig::setSimilaritySkip, GAConfig::getSimilaritySkip,
                        v -> new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, v)),
                new ConfigEntry<>(gaSimilarityThreshold, GAConfig::setSimilarityThreshold, GAConfig::getSimilarityThreshold,
                        v -> new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, v, 0.01))
        );

        gaRunButton.setOnAction(event -> {
            visualizationCurrentGenerationSpinner.getValueFactory().setValue(session.getGeneticAlgorithm().getConfig().getIterationsPerRun());
            visualizationCurrentDnaSpinner.getValueFactory().setValue(1);
            onGAStart();
            session.runGA(result -> {
                requestRenderAll();
            }).whenComplete((listRunResult, throwable) -> {
                onGAEnd();

                if (throwable != null) {
                    if (!(throwable instanceof InterruptedException)) {
                        throwable.printStackTrace();
                    }
                    return;
                }
                visualizationCurrentGenerationSpinner.getValueFactory().setValue(Math.min(
                        visualizationCurrentGenerationSpinner.getValue(),
                        listRunResult.getGenerations().size()
                ));
                requestRenderAll();
            });
        });

        gaStopButton.setOnAction(event -> {
            session.interruptRun();
        });

        gaResetConfig.setOnAction(event -> {
            GAConfig newConfig = GAConfig.builder().build();
            session.getGeneticAlgorithm().setConfig(newConfig);

            gaEntries.forEach(configEntry -> configEntry.setSpinnerValue(newConfig));
        });

        var config = session.getGeneticAlgorithm().getConfig();

        gaEntries.forEach(configEntry -> {
           configEntry.setup(config);
        });


        visualizationCurrentGenerationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000, 2)
        );
        visualizationCurrentGenerationSpinner.setEditable(true);
        visualizationCurrentGenerationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            requestRenderAll();
        });

        visualizationCurrentDnaSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, 1)
        );
        visualizationCurrentDnaSpinner.setEditable(true);
        visualizationCurrentDnaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            requestRenderAll();
        });

        visualizationSkipToResultButton.setOnAction(event -> {
            visualizationCurrentDnaSpinner.getValueFactory().setValue(1);

            int lastGeneration = session.getLastResult() == null
                    ? session.getGeneticAlgorithm().getConfig().getIterationsPerRun()
                    : session.getLastResult().getGenerations().size();
            visualizationCurrentGenerationSpinner.getValueFactory().setValue(lastGeneration);
        });
    }

    private void openSudokuCreateWindow() throws IOException {
        URL fxmlUrl = getClass().getResource("/create-sudoku-view.fxml");

        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find create-sudoku-view.fxml in resources");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);

        Parent root = loader.load();
        CreateSudokuGuiController controller = loader.getController();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create Sudoku");
        dialogStage.setScene(new Scene(root));

        Stage mainStage = (Stage) (sudokuCreateButton).getScene().getWindow();
        dialogStage.initOwner(mainStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);

        controller.setup(sudokuTypeComboBox.getValue() == null ? session.getBoard().getType() : sudokuTypeComboBox.getValue(), sudokuMatrix -> {
            session.setBoard(sudokuMatrix);
            requestRenderAll();
        });

        dialogStage.showAndWait();
    }


    private void saveSudoku() {
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Сохранить судоку");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files", "*.json")
        );

        File file = chooser.showSaveDialog(root.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, session.getBoard().toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSudoku() {
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Загрузить судоку");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files", "*.json")
        );

        File file = chooser.showOpenDialog(root.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            session.setBoard(SudokuBoard.fromJson(jsonMapper.readTree(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderSudoku();
    }

    private void onGAStart() {
        gaStopButton.setDisable(false);
        gaRunButton.setDisable(true);
        sudokuCreateButton.setDisable(true);
        sudokuLoadButton.setDisable(true);
        sudokuGenerateButton.setDisable(true);
    }

    private void onGAEnd() {
        gaStopButton.setDisable(true);
        gaRunButton.setDisable(false);
        sudokuCreateButton.setDisable(false);
        sudokuLoadButton.setDisable(false);
        sudokuGenerateButton.setDisable(false);
    }

    private void requestRenderAll() {
        if (!renderQueued.compareAndSet(false, true)) {
            return;
        }

        Platform.runLater(() -> {
            try {
                renderAll();
            } finally {
                renderQueued.set(false);
            }
        });
    }

    private void renderAll() {
        renderGraph();
        renderSudoku();
        RunResult<byte[]> run = session.getLastResult();

        if (run == null) return;

        String text = """
                Зерно генератора: %d
                Поколение: %d
                Лучший результат: %f (Ошибок: %d)
                Медианный результат: %f
                Средний результат: %f
                
                Результат выбранного: %f (Ошибок: %d)
                """;

        int generation = Math.min(visualizationCurrentGenerationSpinner.getValue(), run.getBestPerGeneration().size());
        double score = run.getBestPerGeneration().get(generation - 1).getScore();

        List<DNAScore<byte[]>> generationDna = run.getGenerations().get(generation - 1);

        double average = generationDna.stream().mapToDouble(DNAScore::getScore).average().orElseThrow();
        double median = generationDna.stream().mapToDouble(DNAScore::getScore).skip(generationDna.size() / 2).findFirst().orElseThrow();

        SudokuBoard topBoard = SudokuUtils.boardFromList(run.getBestPerGeneration().get(generation - 1).getDna());

        DNAScore<byte[]> viewing = getViewingDNA();
        double viewingScore = 0;
        int viewingErrors = 0;

        if (viewing != null) {
            SudokuBoard viewingBoard = SudokuUtils.boardFromList(viewing.getDna());

            viewingErrors = viewingBoard.countUniqueErrors();
            viewingScore = viewing.getScore();

        }

        this.text.setText(String.format(text, run.getSeed(), generation, score, topBoard.countUniqueErrors(), median, average, viewingScore, viewingErrors));
    }

    private void renderGraph() {


        graphSeries.getData().clear();


        RunResult<byte[]> run = session.getLastResult();

        if (run != null) {
            int generations = Math.min(visualizationCurrentGenerationSpinner.getValue(), run.getBestPerGeneration().size());

            int lowerBound = Math.max(0, generations - 100000);

            double lastScore = -1;


            double rawStep = (generations - lowerBound) / (double) 20;

            double stepPower = Math.pow(10, Math.floor(Math.log10(rawStep)));
            double normalizedStep = rawStep / stepPower;

            if (normalizedStep <= 1) {
                normalizedStep = 1;
            } else if (normalizedStep <= 2) {
                normalizedStep = 2;
            } else if (normalizedStep <= 5) {
                normalizedStep = 5;
            } else {
                normalizedStep = 10;
            }
            graphXAxis.setTickUnit((int) (normalizedStep * stepPower));
            graphXAxis.setMinorTickCount(0);


            for (int i = lowerBound; i < generations; i++) {
                DNAScore<byte[]> score = run.getBestPerGeneration().get(i);

                double scoreVal = score.getScore();
                double nextScoreVal = (i == generations - 1) ? scoreVal : run.getBestPerGeneration().get(i + 1).getScore();

                if (i % Math.max(1, i / 100) != 0 && i < generations - 2 && scoreVal != lastScore && nextScoreVal != scoreVal) {
                    continue;
                }

                if (scoreVal != lastScore || (i >= generations - 2) || nextScoreVal != scoreVal) {

                    graphSeries.getData().add(new Data<>(i, score.getScore()));

                    lastScore = scoreVal;
                }
            }


            graphXAxis.setAutoRanging(false);
            graphXAxis.setLowerBound(lowerBound);
            graphXAxis.setUpperBound(generations);
        }

    }

    private void renderSudoku() {
        GraphicsContext gc = sudoku.getGraphicsContext2D();


        double w = sudoku.getWidth();
        double h = sudoku.getHeight();

        gc.clearRect(0, 0, w, h);

        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillRect(0, 0, w, h);

        if (session == null) return;


        SudokuBoard board = session.getBoard();

        double cellWidth = w / board.getWidth();
        double cellHeight = h / board.getHeight();

        gc.setFont(Font.font(cellHeight * 0.6));


        DNAScore<byte[]> viewingDNA = getViewingDNA();

        SudokuMatrixView view = new SudokuMatrixView() {
            @Override
            public int getNumber(int x, int y) {
                int number = board.getNumber(x, y);
                if (number == 0 && viewingDNA != null)
                    number = SudokuUtils.getNumberFromList(viewingDNA.getDna(), x, y);
                return number;
            }

            @Override
            public int getWidth() {
                return board.getWidth();
            }

            @Override
            public int getHeight() {
                return board.getHeight();
            }
        };

        Set<SudokuCell> errorCells = board.getErrorCells(view);


        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (errorCells.contains(new SudokuCell(i, j))) {
                    gc.setFill(Color.RED);
                    gc.fillRect(i * cellWidth, j * cellHeight, cellWidth, cellHeight);
                }

                int number = board.getNumber(i, j);
                if (number == 0) {
                    if (viewingDNA == null) {
                        continue;
                    }


                    number = SudokuUtils.getNumberFromList(viewingDNA.getDna(), i, j);

                    gc.setFill(Color.rgb(150, 50, 50));

                } else {
                    gc.setFill(Color.rgb(50, 50, 50));
                }


                gc.fillText(String.valueOf(number), (i + (number > 9 ? 0.1 : 0.4)) * cellWidth, (j + (0.8)) * cellHeight);
            }
        }

        gc.setStroke(Color.BLACK);

        for (int i = 0; i < board.getWidth() + 1; i++) {
            gc.setLineWidth(i % board.getType().getBlockWidth() == 0 ? 2 : 1);
            gc.strokeLine(i * cellWidth, 0, i * cellWidth, h);
        }

        for (int j = 0; j < board.getHeight() + 1; j++) {
            gc.setLineWidth(j % board.getType().getBlockHeight() == 0 ? 2 : 1);
            gc.strokeLine(0, j * cellHeight, w, j * cellHeight);
        }

    }

    private DNAScore<byte[]> getViewingDNA() {
        RunResult<byte[]> result = session.getLastResult();
        if (result == null || session.isLastResultOutdated()) {
            return null;
        } else {
            int currentGeneration = Math.min(visualizationCurrentGenerationSpinner.getValue() - 1, result.getBestPerGeneration().size() - 1);
            List<DNAScore<byte[]>> dna = result.getGenerations().get(currentGeneration);

            return dna.get(Math.clamp(visualizationCurrentDnaSpinner.getValue() - 1, 0, dna.size() - 1));
        }
    }

    private void bindCanvasSquareToColumn(Canvas canvas, Region canvasContainer, Region column) {
        NumberBinding availableHeight = column.heightProperty().multiply(0.55).subtract(GAP);

        NumberBinding size = Bindings.min(column.widthProperty(), availableHeight);

        canvas.widthProperty().bind(size);
        canvas.heightProperty().bind(size);

        canvasContainer.prefHeightProperty().bind(size);
        canvasContainer.minHeightProperty().bind(size);
        canvasContainer.maxHeightProperty().bind(size);
    }
}