package me.kb.ga.algorithm;

import java.util.List;
import java.util.Random;

public interface GACrossover<DNA> {
    List<DNA> crossover(Random random, DNA parent1, DNA parent2);
}
