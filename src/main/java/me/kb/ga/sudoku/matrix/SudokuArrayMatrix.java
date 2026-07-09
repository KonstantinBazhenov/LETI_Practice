package me.kb.ga.sudoku.matrix;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SudokuArrayMatrix implements SudokuMatrix {
    private final byte[][] board;

    public SudokuArrayMatrix(int[][] board) {
        this.board = new byte[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                this.board[i][j] = (byte) board[i][j];
            }
        }
    }

    @Override
    public void setNumber(int x, int y, int number) {
        board[x][y] = (byte) number;
    }

    @Override
    public SudokuMatrixView getView() {
        return new SudokuMatrixView() {
            @Override
            public int getNumber(int x, int y) {
                return board[x][y];
            }

            @Override
            public int getWidth() {
                return board.length;
            }

            @Override
            public int getHeight() {
                return board[0].length;
            }
        };
    }

    @Override
    public int getNumber(int x, int y) {
        return board[x][y];
    }

    @Override
    public int getWidth() {
        return board.length;
    }

    @Override
    public int getHeight() {
        return board[0].length;
    }

    public SudokuArrayMatrix copy() {
        byte[][] copyBoard = new byte[getWidth()][getHeight()];
        for (int x = 0; x < getWidth(); x++) {
            if (getHeight() >= 0) System.arraycopy(board[x], 0, copyBoard[x], 0, getHeight());
        }

        return new SudokuArrayMatrix(copyBoard);
    }
}
