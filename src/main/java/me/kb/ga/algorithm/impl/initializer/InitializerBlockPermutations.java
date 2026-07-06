package me.kb.ga.algorithm.impl.initializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.kb.ga.algorithm.GAInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class InitializerBlockPermutations implements GAInitializer<List<Integer>> {
    private int blockCount;
    private int blockSize;

    @Override
    public List<List<Integer>> init(Random random, int size) {

        List<List<Integer>> generation = new ArrayList<>();


        for (int i = 0; i < size; i++) {
            List<Integer> dna = new ArrayList<>();

            for (int j = 0; j < blockCount; j++) {
                List<Integer> block = new ArrayList<>();
                for (int s = 1; s <= blockSize; s++) {
                    block.add(s);
                }
                Collections.shuffle(block, random);
                dna.addAll(block);
            }
            generation.add(dna);
        }

        return generation;
    }
}
