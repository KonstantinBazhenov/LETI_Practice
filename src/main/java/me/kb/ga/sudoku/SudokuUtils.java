package me.kb.ga.sudoku;

import lombok.experimental.UtilityClass;
import me.kb.ga.data.SudokuCell;

import java.util.*;

@UtilityClass
public class SudokuUtils {


    public void printBoard(byte[] board) {
        int size = (int) Math.sqrt(board.length);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                System.out.print(" " + board[x * size + y]);
            }
            System.out.println();
        }
    }

    public double getSimilarityScore(byte[] board1, byte[] board2) {
        int size1 = board1.length;
        int size2 = board2.length;
        if (size1 != size2) {
            throw new IllegalArgumentException("Boards must have same size");
        }

        int same = 0;

        for (int i = 0; i < board1.length; i++) {
            if (Objects.equals(board1[i], board2[i])) {
                same++;
            }
        }

        return (double) same / board1.length;
    }

    public int getNumberFromList(List<Integer> board, int x, int y) {
        int size = (int) Math.sqrt(board.size());
        int width = size;
        int height = size;

        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Invalid coordinates: x=" + x + ", y=" + y);
        }

        return board.get(x * height + y);
    }


    public int getNumberFromList(byte[] board, int x, int y) {
        int size = (int) Math.sqrt(board.length);
        int width = size;
        int height = size;

        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Invalid coordinates: x=" + x + ", y=" + y);
        }

        return board[x * height + y];
    }
}
