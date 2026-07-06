package me.kb.ga.algorithm.impl.mutate;

import me.kb.ga.algorithm.GAMutate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MutateDeltaOne implements GAMutate<List<Integer>> {
    @Override
    public List<Integer> mutate(Random random, List<Integer> DNA, double rate) {
        List<Integer> newDNA = new ArrayList<>(DNA);


        for (int i = 0; i < newDNA.size(); i++) {
            if (random.nextDouble() < rate) {
                newDNA.set(i, newDNA.get(i) + (random.nextBoolean() ? 1 : -1));
            }
        }

        return newDNA;
    }
}
