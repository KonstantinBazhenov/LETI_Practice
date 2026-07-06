package me.kb.ga.sudoku.matrix;

public interface SudokuMatrix extends SudokuMatrixView {
    void setNumber(int x, int y, int number);

    SudokuMatrixView getView();

    SudokuMatrix copy();
}
