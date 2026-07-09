package me.kb.ga.main;

import me.kb.ga.algorithm.GeneticAlgorithm;
import me.kb.ga.algorithm.impl.crossover.CrossoverBlocksUniform;
import me.kb.ga.algorithm.impl.initializer.InitializerBlockPermutations;
import me.kb.ga.algorithm.impl.mutate.MutateBlockPermutations;
import me.kb.ga.algorithm.impl.selector.SelectorRankedWeightedRandom;
import me.kb.ga.data.GAConfig;
import me.kb.ga.data.RunResult;
import me.kb.ga.sudoku.*;

import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {


        for (int i = 0; i < 10; i++) {


            SudokuType type = SudokuType.SUDOKU_9;
            SudokuBoard board = new SudokuBoard(SudokuGenerator.generate(new Random(), type, 30));

            SudokuTask task = new SudokuTask(board);


            GeneticAlgorithm<byte[]> geneticAlgorithm = new GeneticAlgorithm<>(
                    GAConfig.builder()
                            .iterationsPerRun(3000)
                            .populationSize(500)
                            .mutationRate(0.09)
                            .crossoverRate(0.75)
                            .copyBestRate(0.02)
                            .similarityThreshold(0.82)
                            .similarityPunishment(2.5)
                            .stagnationKeepRate(0.05)
                            .stagnationGenerations(100)
                            .similaritySkip(3)
                            .similarityCompare(30).build(),
                    new InitializerBlockPermutations(type.getSize(), type.getSize()),
                    new CrossoverBlocksUniform(type.getSize()),
                    new MutateBlockPermutations(type.getSize()),
                    new SelectorRankedWeightedRandom(),
                    task
            );

            RunResult<byte[]> result = geneticAlgorithm.run();
            System.out.println("Best score: " + result.getBest().getScore() + " Generations: " + result.getGenerations().size());
            SudokuUtils.printBoard(result.getBest().getDna());
        }


    }
}
