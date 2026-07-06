package me.kb.ga.algorithm.impl.crossover;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.kb.ga.algorithm.GACrossover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class CrossoverBlocksUniform implements GACrossover<List<Integer>> {
    int blockSize;


    @Override
    public List<List<Integer>> crossover(Random random, List<Integer> dna1, List<Integer> dna2) {

        if (dna1.size() != dna2.size()) {
            throw new IllegalArgumentException("DNA size mismatch");
        }

        if (dna1.size() % blockSize != 0) {
            throw new IllegalArgumentException("DNA size must be divisible by block size");
        }

        int blockCount = dna1.size() / blockSize;


        List<Integer> newDna1 = new ArrayList<>();
        List<Integer> newDna2 = new ArrayList<>();


        Iterator<Integer> iter1 = dna1.iterator();
        Iterator<Integer> iter2 = dna2.iterator();

        for (int i = 0; i < blockCount; i++) {
            if (random.nextBoolean()) {
                for (int j = 0; j < blockSize; j++) {
                    newDna1.add(iter1.next());
                    newDna2.add(iter2.next());
                }
            } else {
                for (int j = 0; j < blockSize; j++) {
                    newDna1.add(iter2.next());
                    newDna2.add(iter1.next());
                }
            }
        }


        return List.of(newDna1, newDna2);
    }
}
