package me.kb.ga.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import me.kb.ga.sudoku.SudokuBoard;
import me.kb.ga.sudoku.SudokuType;
import me.kb.ga.sudoku.matrix.SudokuArrayMatrix;
import me.kb.ga.sudoku.matrix.SudokuMatrix;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CreateSudokuGuiController {

    @FXML
    private Button doneButton;

    @FXML
    private Button cancelButton;

    @FXML
    private GridPane sudokuBoard;

    private SudokuType type;

    private Consumer<SudokuArrayMatrix> onDone;

    private TextField[][] cells;

    @FXML
    private void initialize() {

    }


    public void setup(SudokuType type, Consumer<SudokuArrayMatrix> onDone) {
        this.type = type;
        this.onDone = onDone;

        Platform.runLater(() -> {

            this.sudokuBoard.getChildren().clear();
            this.sudokuBoard.getColumnConstraints().clear();
            this.sudokuBoard.getRowConstraints().clear();

            this.cells = new TextField[type.getSize()][type.getSize()];

            Set<String> allowedText = new HashSet<>();
            allowedText.add("");
            for (int i = 1; i <= type.getSize(); i++) {
                allowedText.add(String.valueOf(i));
            }

            for (int x = 0; x < type.getSize(); x++) {
                for (int y = 0; y < type.getSize(); y++) {
                    TextField cell = new TextField();
                    cell.setPrefSize(40, 40);
                    cell.setAlignment(Pos.CENTER);

                    cell.setTextFormatter(new TextFormatter<>(change -> {
                        String text = change.getControlNewText();


                        if (!allowedText.contains(text)) {
                            return null;
                        }

                        return change;
                    }));

                    cells[x][y] = cell;

                    sudokuBoard.add(cell, x, y);
                }
            }


            doneButton.setOnAction(event -> {
                int[][] board = new int[type.getSize()][type.getSize()];


                for (int x = 0; x < type.getSize(); x++) {
                    for (int y = 0; y < type.getSize(); y++) {
                        String value = cells[x][y].getText();

                        if (value != null && !value.isBlank()) {
                            try {
                                board[x][y] = Integer.parseInt(value);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }


                SudokuArrayMatrix matrix = new SudokuArrayMatrix(board);

                SudokuBoard sudokuBoard = new SudokuBoard(matrix);

                if (!sudokuBoard.isValid()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("Введенное судоку некорректно");
                    alert.showAndWait();
                    return;
                }

                onDone.accept(matrix);


                Stage stage = (Stage) doneButton.getScene().getWindow();
                stage.close();
            });

            cancelButton.setOnAction(event -> {
                Stage stage = (Stage) doneButton.getScene().getWindow();
                stage.close();
            });
        });
    }

}
