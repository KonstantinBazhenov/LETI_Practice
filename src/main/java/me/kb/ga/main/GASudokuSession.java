package me.kb.ga.main;

import lombok.Getter;
import me.kb.ga.algorithm.GeneticAlgorithm;
import me.kb.ga.algorithm.impl.crossover.CrossoverBlocksUniform;
import me.kb.ga.algorithm.impl.initializer.InitializerBlockPermutations;
import me.kb.ga.algorithm.impl.mutate.MutateBlockPermutations;
import me.kb.ga.algorithm.impl.selector.SelectorRankedWeightedRandom;
import me.kb.ga.data.GAConfig;
import me.kb.ga.data.RunResult;
import me.kb.ga.sudoku.SudokuBoard;
import me.kb.ga.sudoku.SudokuGenerator;
import me.kb.ga.sudoku.SudokuTask;
import me.kb.ga.sudoku.SudokuType;
import me.kb.ga.sudoku.matrix.SudokuMatrix;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GASudokuSession {
    @Getter
    private SudokuBoard board;
    @Getter
    private GeneticAlgorithm<List<Integer>> geneticAlgorithm;
    @Getter
    private RunResult<List<Integer>> lastResult;
    @Getter
    private boolean lastResultOutdated;
    private SudokuTask task;
    private CompletableFuture<RunResult<List<Integer>>> runFuture;

    public GASudokuSession(GAConfig config, SudokuType type) {
        regenerateBoard(type, 30);

        task = new SudokuTask(board);

        geneticAlgorithm = new GeneticAlgorithm<>(
                new Random(),
                config,
                new InitializerBlockPermutations(type.getSize(), type.getSize()),
                new CrossoverBlocksUniform(type.getSize()),
                new MutateBlockPermutations(type.getSize()),
                new SelectorRankedWeightedRandom(),
                task
        );


    }


    public synchronized CompletableFuture<RunResult<List<Integer>>> runGA() {
        return runGA(null);
    }

    public synchronized CompletableFuture<RunResult<List<Integer>>> runGA(Consumer<RunResult<List<Integer>>> iterationConsumer) {
        if (runFuture != null) {
            return runFuture;
        }
        runFuture = new CompletableFuture<>();
        new Thread(() -> {
            try {
                RunResult<List<Integer>> result = geneticAlgorithm.run(listRunResult -> {
                    lastResult = listRunResult;
                    this.lastResultOutdated = false;
                    if (iterationConsumer != null)
                        iterationConsumer.accept(listRunResult);
                });
                this.lastResult = result;
                this.lastResultOutdated = false;
                runFuture.complete(result);
            } catch (Throwable t) {
                runFuture.completeExceptionally(t);
            }
            runFuture = null;
        }, "GA Executor").start();

        return runFuture;
    }

    public void regenerateBoard(int keepNumbers) {
        regenerateBoard(board.getType(), keepNumbers);
    }

    public void regenerateBoard(SudokuType type, int keepNumbers) {
        setBoard(SudokuGenerator.generate(new Random(), type, keepNumbers));
    }

    public void setBoard(SudokuMatrix matrix) {
        setBoard(new SudokuBoard(matrix));
    }

    public synchronized boolean isGAInProgress() {
        return this.runFuture != null && !this.runFuture.isDone();
    }

    public synchronized void setBoard(SudokuBoard board) {
        if (isGAInProgress()) {
            throw new IllegalStateException("GA is in progress, can't change board.");
        }
        this.board = board;
        if (task != null)
            task.updateFixed(board);
        lastResultOutdated = true;

        SudokuType type = board.getType();

        if (geneticAlgorithm != null) {
            geneticAlgorithm = new GeneticAlgorithm<>(
                    new Random(),
                    geneticAlgorithm.getConfig(),
                    new InitializerBlockPermutations(type.getSize(), type.getSize()),
                    new CrossoverBlocksUniform(type.getSize()),
                    new MutateBlockPermutations(type.getSize()),
                    new SelectorRankedWeightedRandom(),
                    task
            );
        }
    }
}
