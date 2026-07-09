package me.kb.ga.sudoku;

import me.kb.ga.algorithm.GATask;
import me.kb.ga.data.DNAScore;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SudokuTask implements GATask<List<Integer>> {
    private final Map<List<Integer>, Double> evalCache = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> fixed;
    private SudokuType type;

    public SudokuTask(SudokuBoard sudokuBoard) {
        this.type = sudokuBoard.getType();
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

    public void updateFixed(SudokuBoard sudokuBoard) {
        this.type = sudokuBoard.getType();
        this.fixed.clear();
        for (int i = 0; i < sudokuBoard.getWidth(); i++) {
            for (int j = 0; j < sudokuBoard.getHeight(); j++) {
                int number = sudokuBoard.getNumber(i, j);

                if (number != 0) {
                    fixed.put(i * sudokuBoard.getHeight() + j, number);
                }
            }
        }
        evalCache.clear();
    }

    @Override
    public double eval(List<Integer> numbers) {
        if (evalCache.size() > 1000) {
            List<List<Integer>> keys = evalCache.keySet().stream().limit(100).toList();
            keys.forEach(evalCache.keySet()::remove);

            evalCache.remove(keys);
        }
        return evalCache.computeIfAbsent(numbers, integers -> {
            SudokuBoard board = new SudokuBoard(type);
            setBoard(board, numbers);
            return Math.exp(-0.1 * board.countUniqueErrors());
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
    public List<Integer> tryCorrect(List<Integer> numbers) {
        if (isCorrect(numbers)) return numbers;

        List<Integer> newNumbers = new ArrayList<>(numbers);

        for (int i = 0; i < type.getSize(); i++) {
            for (int j = 0; j < type.getSize(); j++) {
                int index = i * type.getSize() + j;

                int number = Math.clamp(newNumbers.get(index), 1, type.getSize());
                int fixedOrNumber = fixed.getOrDefault(index, number);

                if (fixedOrNumber != number) {

                    boolean fail = true;
                    for (int k = 0; k < type.getSize(); k++) {
                        int index2 = i * type.getSize() + k;
                        if (newNumbers.get(index2) == fixedOrNumber) {
                            newNumbers.set(index2, number);
                            newNumbers.set(index, fixedOrNumber);
                            fail = false;
                            break;
                        }
                    }
                    if (fail) {
                        List<Integer> column = new ArrayList<>();
                        for (int k = 0; k < type.getSize(); k++) {
                            int index2 = i * type.getSize() + k;
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


    private void setBoard(SudokuBoard sudokuBoard, List<Integer> numbers) {
        if (numbers.size() != (type.getSize() * type.getSize()))
            throw new IllegalArgumentException("Invalid number count");

        Iterator<Integer> iterator = numbers.iterator();
        for (int x = 0; x < sudokuBoard.getWidth(); x++) {
            for (int y = 0; y < sudokuBoard.getHeight(); y++) {
                sudokuBoard.setNumber(x, y, iterator.next(), true, true);
            }
        }
    }
}
