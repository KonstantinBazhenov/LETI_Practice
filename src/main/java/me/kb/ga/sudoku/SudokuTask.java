package me.kb.ga.sudoku;

import lombok.AllArgsConstructor;
import me.kb.ga.algorithm.GATask;
import me.kb.ga.data.DNAScore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SudokuTask implements GATask<List<Integer>> {
    private final Map<List<Integer>, Double> evalCache = new ConcurrentHashMap<>();
    private final SudokuBoard sudokuBoard;
    private final Map<Integer, Integer> fixed;

    public SudokuTask(SudokuBoard sudokuBoard) {
        this.sudokuBoard = sudokuBoard;
        this.fixed = new HashMap<>();
        for (int i = 0; i < sudokuBoard.getWidth(); i++) {
            for (int j = 0; j < sudokuBoard.getHeight(); j++) {
                int number = sudokuBoard.getNumber(i, j);

                if (number != 0) {
                    fixed.put(i * sudokuBoard.getHeight() + j, number);
                }
            }
        }
    }

    @Override
    public synchronized double eval(List<Integer> numbers) {
        return evalCache.computeIfAbsent(numbers, integers -> {
            setBoard(numbers);
            return 1 / Math.pow(1 + sudokuBoard.countErrors(), 1);
        });
    }

    @Override
    public boolean shouldStop(DNAScore<List<Integer>> best) {
        return best.getScore() >= 1;
    }

    @Override
    public boolean isCorrect(List<Integer> numbers) {
        for (int i = 0; i < numbers.size(); i++) {
            int number = numbers.get(i);
            if (number < 0 || number > sudokuBoard.getType().getSize()) {
                return false;
            }
            if (fixed.getOrDefault(i, number)  != number) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Integer> tryCorrect(List<Integer> numbers) {
        if (isCorrect(numbers)) return numbers;

        List<Integer> newNumbers = new ArrayList<>(numbers);

        for (int i = 0; i < sudokuBoard.getWidth(); i++) {
            for (int j = 0; j < sudokuBoard.getHeight(); j++) {
                int index = i * sudokuBoard.getHeight() + j;

                int number = Math.clamp(newNumbers.get(index), 1, sudokuBoard.getType().getSize());
                int fixedOrNumber = fixed.getOrDefault(index, number);

                if (fixedOrNumber != number) {

                    boolean fail = true;
                    for (int k = 0; k < sudokuBoard.getHeight(); k++) {
                        int index2 = i * sudokuBoard.getHeight() + k;
                        if (newNumbers.get(index2) == fixedOrNumber) {
                            newNumbers.set(index2, number);
                            newNumbers.set(index, fixedOrNumber);
                            fail = false;
                            break;
                        }
                    }
                    if (fail) {
                        List<Integer> column = new ArrayList<>();
                        for (int k = 0; k < sudokuBoard.getHeight(); k++) {
                            int index2 = i * sudokuBoard.getHeight() + k;
                            column.add(newNumbers.get(index2));
                        }
                        System.out.println("Failed to find a proper swap for fixed number " + column);
                    }

                } else {
                    newNumbers.set(index, number);
                }
            }
        }

        return newNumbers;
    }

    @Override
    public double getSimilarity(List<Integer> dna1, List<Integer> dna2) {
        return SudokuUtils.getSimilarityScore(dna1, dna2);
    }


    private void setBoard(List<Integer> numbers) {
        if (numbers.size() != (sudokuBoard.getHeight() * sudokuBoard.getWidth()))
            throw new IllegalArgumentException("Invalid number count");

        Iterator<Integer> iterator = numbers.iterator();
        for (int x = 0; x < sudokuBoard.getWidth(); x++) {
            for (int y = 0; y < sudokuBoard.getHeight(); y++) {
                sudokuBoard.setNumber(x, y, iterator.next(), true, true);
            }
        }
    }
}
