package me.kb.ga.sudoku;

import lombok.experimental.UtilityClass;
import me.kb.ga.data.SudokuCell;
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

        List<Integer> numbers = shuffledRange(random, 1, size + 1);
        List<Integer> rows = shuffledBlocks(random, size, type.getBlockHeight());
        List<Integer> cols = shuffledBlocks(random, size, type.getBlockWidth());

        int[][] board = new int[size][size];

        for (int y = 0; y < size; y++) {
            int sourceY = rows.get(y);

            for (int x = 0; x < size; x++) {
                int sourceX = cols.get(x);

                int valueIndex = (sourceY * type.getBlockWidth() + sourceY / type.getBlockHeight() + sourceX) % size;
                board[x][y] = numbers.get(valueIndex);
            }
        }

        List<SudokuCell> cells = new ArrayList<>();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                cells.add(new SudokuCell(x, y));
            }
        }

        Collections.shuffle(cells, random);

        int targetRemoved = size * size - keepNumbers;

        for (int i = 0; i < targetRemoved; i++) {
            SudokuCell cell = cells.get(i);

            board[cell.x()][cell.y()] = 0;
        }

        return new SudokuArrayMatrix(board);
    }

    private List<Integer> shuffledBlocks(Random random, int size, int blockSize) {
        List<Integer> blocks = shuffledRange(random, 0, size / blockSize);
        List<Integer> result = new ArrayList<>(size);

        for (int block : blocks) {
            List<Integer> insideBlock = shuffledRange(random, 0, blockSize);

            for (int offset : insideBlock) {
                result.add(block * blockSize + offset);
            }
        }

        return result;
    }

    private List<Integer> shuffledRange(Random random, int from, int to) {
        List<Integer> list = new ArrayList<>();

        for (int i = from; i < to; i++) {
            list.add(i);
        }

        Collections.shuffle(list, random);
        return list;
    }
}
