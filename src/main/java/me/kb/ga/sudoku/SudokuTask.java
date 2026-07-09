package me.kb.ga.sudoku;

import me.kb.ga.algorithm.GATask;
import me.kb.ga.data.DNAScore;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SudokuTask implements GATask<byte[]> {
    private final Map<Integer, Byte> fixed;
    private SudokuType type;

    public SudokuTask(SudokuBoard sudokuBoard) {
        this.type = sudokuBoard.getType();
        this.fixed = new HashMap<>();
        for (int i = 0; i < sudokuBoard.getWidth(); i++) {
            for (int j = 0; j < sudokuBoard.getHeight(); j++) {
                int number = sudokuBoard.getNumber(i, j);

                if (number != 0) {
                    fixed.put(i * sudokuBoard.getHeight() + j, (byte) number);
                }
            }
        }
    }

    public void updateFixed(SudokuBoard sudokuBoard) {
        this.type = sudokuBoard.getType();
        this.fixed.clear();
        for (int i = 0; i < sudokuBoard.getWidth(); i++) {
            for (int j = 0; j < sudokuBoard.getHeight(); j++) {
                int number = sudokuBoard.getNumber(i, j);

                if (number != 0) {
                    fixed.put(i * sudokuBoard.getHeight() + j, (byte) number);
                }
            }
        }
    }

    @Override
    public double eval(byte[] numbers) {
        SudokuBoard board = new SudokuBoard(type);
        setBoard(board, numbers);
        return Math.exp(-0.1 * board.countUniqueErrors());
    }

    @Override
    public boolean shouldStop(DNAScore<byte[]> best) {
        return best.getScore() >= 1;
    }

    @Override
    public boolean isCorrect(byte[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            byte number = numbers[i];
            if (number < 0 || number > type.getSize()) {
                return false;
            }
            if (fixed.getOrDefault(i, number) != number) {
                return false;
            }
        }
        return true;
    }

    @Override
    public byte[] tryCorrect(byte[] numbers) {
        if (isCorrect(numbers)) return numbers;

        byte[] newNumbers = new byte[numbers.length];
        System.arraycopy(numbers, 0, newNumbers, 0, numbers.length);


        for (int i = 0; i < type.getSize(); i++) {
            for (int j = 0; j < type.getSize(); j++) {
                int index = i * type.getSize() + j;

                byte number = (byte) Math.clamp(newNumbers[index], 1, type.getSize());
                byte fixedOrNumber = fixed.getOrDefault(index, number);

                if (fixedOrNumber != number) {

                    boolean fail = true;
                    for (int k = 0; k < type.getSize(); k++) {
                        int index2 = i * type.getSize() + k;
                        if (newNumbers[index2] == fixedOrNumber) {
                            newNumbers[index2] = number;
                            newNumbers[index] = fixedOrNumber;
                            fail = false;
                            break;
                        }
                    }
                    if (fail) {
                        System.out.println("Failed to find a proper swap for fixed number");
                    }

                } else {
                    newNumbers[index] = number;
                }
            }
        }

        return newNumbers;
    }

    @Override
    public double getSimilarity(byte[] dna1, byte[] dna2) {
        return SudokuUtils.getSimilarityScore(dna1, dna2);
    }


    private void setBoard(SudokuBoard sudokuBoard, byte[] numbers) {
        if (numbers.length != (type.getSize() * type.getSize()))
            throw new IllegalArgumentException("Invalid number count");

        for (int x = 0; x < sudokuBoard.getWidth(); x++) {
            for (int y = 0; y < sudokuBoard.getHeight(); y++) {
                int number = SudokuUtils.getNumberFromList(numbers, x, y);
                sudokuBoard.setNumber(x, y, number, true, true);
            }
        }
    }
}
