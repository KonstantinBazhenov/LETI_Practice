package me.kb.ga.sudoku;

import lombok.experimental.UtilityClass;
import me.kb.ga.sudoku.matrix.SudokuArrayMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@UtilityClass
public class SudokuGenerator {

    public SudokuArrayMatrix generate(Random random, SudokuType type, int keepNumbers) {
        keepNumbers = Math.clamp(keepNumbers, 0, type.getSize() * type.getSize());

        int size = type.getSize();
        int[][] board = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                board[x][y] = (y * type.getBlockWidth() + y / type.getBlockHeight() + x) % size + 1;
            }
        }

        List<int[]> cells = new ArrayList<>();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                cells.add(new int[]{x, y});
            }
        }

        Collections.shuffle(cells, random);

        int targetRemoved = size * size - keepNumbers;

        for (int i = 0; i < targetRemoved; i++) {
            int[] cell = cells.get(i);
            int x = cell[0];
            int y = cell[1];

            board[x][y] = 0;
        }

        return new SudokuArrayMatrix(board);
    }
}
