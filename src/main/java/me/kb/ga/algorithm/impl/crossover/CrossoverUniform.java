package me.kb.ga.algorithm.impl.crossover;

import me.kb.ga.algorithm.GACrossover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CrossoverUniform implements GACrossover<List<Integer>> {

    @Override
    public List<List<Integer>> crossover(Random random, List<Integer> dna1, List<Integer> dna2) {

        if (dna1.size() != dna2.size()) {
            throw new IllegalArgumentException("DNA size mismatch");
        }

        List<Integer> newDna1 = new ArrayList<>();
        List<Integer> newDna2 = new ArrayList<>();


        Iterator<Integer> iter1 = dna1.iterator();
        Iterator<Integer> iter2 = dna2.iterator();

        for (int i = 0; i < dna1.size(); i++) {
            if (random.nextBoolean()) {
                newDna1.add(iter1.next());
                newDna2.add(iter2.next());
            } else {
                newDna1.add(iter2.next());
                newDna2.add(iter1.next());
            }
        }


        return List.of(newDna1, newDna2);
    }
}
