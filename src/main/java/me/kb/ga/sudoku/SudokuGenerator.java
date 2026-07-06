package me.kb.ga.sudoku;

import lombok.experimental.UtilityClass;
import me.kb.ga.sudoku.matrix.SudokuArrayMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@UtilityClass
public class SudokuGenerator {

    public SudokuBoard generate(Random random, int keepNumbers) {
        int[][] board = new int[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                board[row][col] = (row * 3 + row / 3 + col) % 9 + 1;
            }
        }

        List<int[]> cells = new ArrayList<>();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells.add(new int[]{r, c});
            }
        }

        Collections.shuffle(cells, random);

        int removed = 0;
        int targetRemoved = 9 * 9 - keepNumbers;

        for (int[] cell : cells) {
            if (removed >= targetRemoved) break;
            board[cell[0]][cell[1]] = 0;

            removed++;
        }

        return new SudokuBoard(new SudokuArrayMatrix(board));
    }
}
