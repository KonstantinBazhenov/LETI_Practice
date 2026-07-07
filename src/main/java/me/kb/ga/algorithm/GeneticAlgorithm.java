package me.kb.ga.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.kb.ga.data.DNAScore;
import me.kb.ga.data.GAConfig;
import me.kb.ga.data.RunResult;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
@Setter
public class GeneticAlgorithm<DNA> {
    private @NonNull Random random;
    private @NonNull GAConfig config;
    private @NonNull GAInitializer<DNA> initializer;
    private @NonNull GACrossover<DNA> crossover;
    private @NonNull GAMutate<DNA> mutate;
    private @NonNull GASelector selector;
    private @NonNull GATask<DNA> task;


    public RunResult<DNA> run() {
        return run(null);
    }

    public RunResult<DNA> run(Consumer<RunResult<DNA>> iterationConsumer) {

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

        if (!config.isValid()) {
            throw new InvalidParameterException("Invalid configuration");
        }

        List<DNAScore<DNA>> bestPerGeneration = new ArrayList<>();
        List<List<DNAScore<DNA>>> generations = new ArrayList<>();

        List<DNA> population = new ArrayList<>(initializer.init(random, config.getPopulationSize()));
        population.replaceAll(dna -> task.tryCorrect(dna));

        DNAScore<DNA> currentBest = null;
        int stagnationCount = 0;

        for (int j = 0; j < config.getIterationsPerRun(); j++) {

            List<DNAScore<DNA>> evaluated = evalAll(population, executor);

            generations.add(evaluated);

            DNAScore<DNA> best = evaluated.get(0);

            if (currentBest == null || best.getScore() > currentBest.getScore()) {
                currentBest = best;
                stagnationCount = 0;
            } else {
                stagnationCount++;
            }

            bestPerGeneration.add(best);


            if (task.shouldStop(best)) {
                break;
            }

            if (stagnationCount >= config.getStagnationGenerations()) {
                int keep = Math.max(1, config.getStagnationKeep());

                List<DNA> newPopulation = new ArrayList<>(evaluated.stream().limit(keep).map(DNAScore::getDna).toList());

                List<DNA> newDna = new ArrayList<>(initializer.init(random, config.getPopulationSize() - newPopulation.size()));

                newDna.replaceAll(task::tryCorrect);
                newPopulation.addAll(newDna);

                population = newPopulation;
                stagnationCount = 0;
            } else {

                List<DNA> newPopulation = new ArrayList<>(evaluated.stream().distinct().limit(config.getCopyBest()).map(DNAScore::getDna).toList());

                newPopulation.addAll(selector.select(random, evaluated, config.getPopulationSize() - newPopulation.size() - config.getCrossover()));

                while (newPopulation.size() < config.getPopulationSize()) {
                    int first = random.nextInt(newPopulation.size());
                    int second = random.nextInt(newPopulation.size());


                    crossover.crossover(random, newPopulation.get(first), newPopulation.get(second)).forEach(dna ->
                            newPopulation.add(mutate.mutate(random, dna, config.getMutationRate())));
                }

                while (newPopulation.size() > config.getPopulationSize()) {
                    newPopulation.remove(newPopulation.size() - 1);
                }

                newPopulation.replaceAll(dna -> task.tryCorrect(dna));

                population = newPopulation;

                if (iterationConsumer != null) {
                    iterationConsumer.accept(new RunResult<>(currentBest, bestPerGeneration, generations));
                }

            }


        }


        executor.shutdown();

        return new RunResult<>(currentBest, bestPerGeneration, generations);
    }

    private List<DNAScore<DNA>> evalAll(List<DNA> population, ExecutorService executor) {
        List<DNAScore<DNA>> evaluated = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(population.size());

        for (DNA dna : population) {
            executor.execute(() -> {
                try {
                    evaluated.add(new DNAScore<>(dna, task.eval(dna)));
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        evaluated.sort(Comparator.comparingDouble(DNAScore<DNA>::getScore).reversed());

        /*
        for (int i = 0; i < population.size() - config.getSimilarityBatch(); i += config.getSimilarityBatch()) {
            for (int j = 1; j <= config.getSimilarityBatch(); j++) {
                double similarity = task.getSimilarity(evaluated.get(i).getDna(), evaluated.get(j + i).getDna());

                if (similarity > config.getSimilarityThreshold()) {
                    double adjustedScore = evaluated.get(j + i).getScore() / (1.0 + similarity * config.getSimilarityPunishment());
                    evaluated.get(j + i).setScore(adjustedScore);
                }
            }
        }*/
        int compareLimit = Math.min(config.getSimilarityCompare(), evaluated.size());


        if (compareLimit > 0) {

            for (int i = config.getSimilaritySkip(); i < population.size(); i++) {
                double similarity = 0;
                for (int j = 0; j < compareLimit; j++) {
                    if (j == i) continue;
                    similarity = Math.max(similarity, task.getSimilarity(evaluated.get(i).getDna(), evaluated.get(j).getDna()));
                }

                if (similarity > config.getSimilarityThreshold()) {
                    double adjustedScore = evaluated.get(i).getScore() / (1.0 + similarity * config.getSimilarityPunishment());
                    evaluated.get(i).setScore(adjustedScore);
                }
            }
        }


        evaluated.sort(Comparator.comparingDouble(DNAScore<DNA>::getScore).reversed());

        return evaluated;
    }

}
