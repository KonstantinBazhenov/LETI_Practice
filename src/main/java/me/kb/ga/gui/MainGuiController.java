package me.kb.ga.gui;

import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import me.kb.ga.data.DNAScore;
import me.kb.ga.data.RunResult;
import me.kb.ga.data.SudokuCell;
import me.kb.ga.main.GASudokuSession;
import me.kb.ga.sudoku.SudokuBoard;
import me.kb.ga.sudoku.SudokuGenerator;
import me.kb.ga.sudoku.SudokuType;
import me.kb.ga.sudoku.SudokuUtils;
import me.kb.ga.sudoku.matrix.SudokuMatrixView;

import java.io.File;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MainGuiController {
    private static final JsonMapper jsonMapper = new JsonMapper();
    private static final double MIN_LEFT_WIDTH = 300.0;
    private static final double MIN_RIGHT_WIDTH = 220.0;

    private static final double MIN_BOTTOM_HEIGHT = 100.0;

    private static final double MIN_GRAPH_SIZE = 100.0;
    private static final double MIN_SUDOKU_SIZE = 100.0;

    private static final double GAP = 10.0;
    private static final double PADDING = 10.0;

    private static final double MIN_SCENE_WIDTH = MIN_LEFT_WIDTH + MIN_RIGHT_WIDTH + GAP + PADDING * 2;

    private static final double MIN_SCENE_HEIGHT = MIN_GRAPH_SIZE + MIN_BOTTOM_HEIGHT + GAP + PADDING * 2;

    private GASudokuSession session;

    @FXML
    private GridPane root;

    @FXML
    private VBox leftColumn;

    @FXML
    private VBox rightColumn;

    @FXML
    private StackPane graphContainer;

    @FXML
    private LineChart<Number, Number> graph;

    @FXML
    private NumberAxis graphXAxis;

    private final XYChart.Series<Number, Number> graphSeries = new XYChart.Series<>();

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
    private Button sudokuSaveButton;

    @FXML
    private Button sudokuLoadButton;

    @FXML
    private Button gaRunButton;

    @FXML
    private Spinner<Integer> visualizationCurrentGenerationSpinner;

    @FXML
    private Spinner<Integer> visualizationCurrentDnaSpinner;

    @FXML
    private Button visualizationSkipToResultButton;


    @FXML
    private void initialize() {
        installStageMinSize();

        bindCanvasSquareToColumn(sudoku, sudokuContainer, rightColumn, MIN_SUDOKU_SIZE);

        sudoku.widthProperty().addListener((obs, oldVal, newVal) -> renderSudoku());
        sudoku.heightProperty().addListener((obs, oldVal, newVal) -> renderSudoku());

        graph.getData().add(graphSeries);


    }

    public void setSession(GASudokuSession session) {
        this.session = session;

        Platform.runLater(() -> {
            setupConfig();
            renderGraph();
            renderSudoku();
        });
    }

    private void setupConfig() {
        int boardSize = session.getBoard().getType().getSize();

        sudokuTypeComboBox.getItems().setAll(SudokuType.values());
        sudokuTypeComboBox.setValue(session.getBoard().getType());

        sudokuTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
           if (newVal != null) {
               int maxCells = newVal.getSize() * newVal.getSize();

               int clamped =  Math.clamp(sudokuFilledCellsSpinner.getValue(), 0, maxCells);
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
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, boardSize * boardSize, Math.clamp(30, 0, boardSize * boardSize))
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

        sudokuLoadButton.setOnAction(event -> loadSudoku());
        sudokuSaveButton.setOnAction(event -> saveSudoku());



        gaRunButton.setOnAction(event -> {
            session.runGA(result -> {
                Platform.runLater(this::renderAll);
            }).whenComplete((listRunResult, throwable) -> {
                Platform.runLater(this::renderAll);
            });
        });


        visualizationCurrentGenerationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 2)
        );
        visualizationCurrentGenerationSpinner.setEditable(true);
        visualizationCurrentGenerationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            renderAll();
        });

        visualizationCurrentDnaSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1)
        );
        visualizationCurrentDnaSpinner.setEditable(true);
        visualizationCurrentDnaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            renderAll();
        });

        visualizationSkipToResultButton.setOnAction(event -> {
           visualizationCurrentDnaSpinner.getValueFactory().setValue(1);

           int lastGeneration = session.getLastResult() == null
                   ? session.getGeneticAlgorithm().getConfig().getIterationsPerRun()
                   : session.getLastResult().getGenerations().size();
           visualizationCurrentGenerationSpinner.getValueFactory().setValue(lastGeneration);
        });
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

    private void renderAll() {
        renderGraph();
        renderSudoku();
    }

    private void renderGraph() {


        graphSeries.getData().clear();


        RunResult<List<Integer>> run = session.getLastResult();

        if (run != null) {
            int generations = Math.min(visualizationCurrentGenerationSpinner.getValue(), run.getBestPerGeneration().size());

            int lowerBound = Math.max(0, generations - 500);

            for (int i = lowerBound; i < generations; i++) {
                DNAScore<List<Integer>> score = run.getBestPerGeneration().get(i);

                graphSeries.getData().add(new Data<>(i, score.getScore()));
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
        RunResult<List<Integer>> result = session.getLastResult();

        DNAScore<List<Integer>> viewingDNA;

        if (result == null || session.isLastResultOutdated()) {
            viewingDNA = null;
        } else {
            int currentGeneration = Math.min(visualizationCurrentGenerationSpinner.getValue() - 1, result.getBestPerGeneration().size() - 1);
            List<DNAScore<List<Integer>>> dna = result.getGenerations().get(currentGeneration);

            viewingDNA = dna.get(Math.clamp(visualizationCurrentDnaSpinner.getValue() - 1, 0, dna.size() - 1));
        }

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
                int number = board.getNumber(i, j);
                if (number == 0) {
                    if (viewingDNA == null) {
                        continue;
                    }

                    if (errorCells.contains(new SudokuCell(i, j))) {
                        gc.setFill(Color.RED);
                        gc.fillRect(i * cellWidth, j * cellWidth, cellWidth, cellHeight);
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
            gc.setLineWidth(i % board.getType().getBlockWidth() == 0 ? 4 : 2);
            gc.strokeLine(i * cellWidth, 0, i * cellWidth, h);
        }

        for (int j = 0; j < board.getHeight() + 1; j++) {
            gc.setLineWidth(j % board.getType().getBlockHeight() == 0 ? 4 : 2);
            gc.strokeLine(0, j * cellHeight, w, j * cellHeight);
        }

    }

    private void bindCanvasSquareToColumn(Canvas canvas, Region canvasContainer, Region column, double minCanvasSize) {
        NumberBinding availableHeight = column.heightProperty().subtract(GAP).subtract(MIN_BOTTOM_HEIGHT);

        NumberBinding size = Bindings.max(minCanvasSize, Bindings.min(column.widthProperty(), availableHeight));

        canvas.widthProperty().bind(size);
        canvas.heightProperty().bind(size);

        canvasContainer.prefHeightProperty().bind(size);
        canvasContainer.minHeightProperty().bind(size);
        canvasContainer.maxHeightProperty().bind(size);
    }

    private void installStageMinSize() {
        root.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                if (newWindow instanceof Stage stage) {
                    Platform.runLater(() -> {
                        double decorationWidth = stage.getWidth() - newScene.getWidth();
                        double decorationHeight = stage.getHeight() - newScene.getHeight();

                        stage.setMinWidth(MIN_SCENE_WIDTH + Math.max(0, decorationWidth));
                        stage.setMinHeight(MIN_SCENE_HEIGHT + Math.max(0, decorationHeight));
                    });
                }
            });
        });
    }
}