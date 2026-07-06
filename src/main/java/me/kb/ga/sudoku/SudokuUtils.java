package me.kb.ga.sudoku;

import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.List;

@UtilityClass
public class SudokuUtils {


    public void printBoard(List<Integer> board) {
        int size = (int) Math.sqrt(board.size());

        Iterator<Integer> iterator = board.iterator();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                System.out.print(" " + iterator.next());
            }
            System.out.println();
        }
    }

    public double getSimilarityScore(List<Integer> board1, List<Integer> board2) {
        int size1 = board1.size();
        int size2 = board2.size();
        if (size1 != size2) {
            throw new IllegalArgumentException("Boards must have same size");
        }

        int diff = 0;
        for (int i = 0; i < size1; i++) {
            diff += Math.abs(board1.get(i) - board2.get(i));
        }

        int maxDiff = ((int) Math.sqrt(size1)) * size1;

        return 1 - (double) diff / (maxDiff);
    }
}
