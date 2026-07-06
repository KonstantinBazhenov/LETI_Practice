package me.kb.ga.main;

import me.kb.ga.algorithm.GeneticAlgorithm;
import me.kb.ga.algorithm.impl.crossover.CrossoverBlocksUniform;
import me.kb.ga.algorithm.impl.initializer.InitializerBlockPermutations;
import me.kb.ga.algorithm.impl.mutate.MutateBlockPermutations;
import me.kb.ga.algorithm.impl.selector.SelectorRankedWeightedRandom;
import me.kb.ga.data.GAConfig;
import me.kb.ga.data.GeneticAlgorithmResult;
import me.kb.ga.sudoku.*;

import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {


        for (int i = 0; i < 10; i++) {


            SudokuType type = SudokuType.SUDOKU_9;
            SudokuBoard board = SudokuGenerator.generate(new Random(), 30);

            SudokuTask task = new SudokuTask(board);


            GeneticAlgorithm<List<Integer>> geneticAlgorithm = new GeneticAlgorithm<>(
                    new Random(),
                    GAConfig.builder()
                            .maxRuns(1)
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

            GeneticAlgorithmResult<List<Integer>> result = geneticAlgorithm.run();
            System.out.println("Best score: " + result.getBest().getScore() + " Runs: " + result.getRuns().size()
                    + " Last run generations: " + result.getRuns().get(result.getRuns().size() - 1).getGenerations().size());
            SudokuUtils.printBoard(result.getBest().getDna());
        }


    }
}
