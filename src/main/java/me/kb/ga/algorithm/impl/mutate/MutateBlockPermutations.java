package me.kb.ga.algorithm.impl.mutate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.kb.ga.algorithm.GAMutate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class MutateBlockPermutations implements GAMutate<List<Integer>> {
    int blockSize;

    @Override
    public List<Integer> mutate(Random random, List<Integer> DNA, double rate) {
        List<Integer> newDNA = new ArrayList<>(DNA);

        if (DNA.size() % blockSize != 0) {
            throw new IllegalArgumentException("DNA size must be divisible by block size");
        }

        int blockCount = DNA.size() / blockSize;

        int bound = (int) (DNA.size() * rate);
        if (bound <= 0) return newDNA;

        int permutations = random.nextInt(bound);

        for (int i = 0; i < permutations; i++) {
            int blockIndex = random.nextInt(blockCount);
            int pos1 = random.nextInt(blockSize) + blockIndex * blockSize;
            int pos2 = random.nextInt(blockSize) + blockIndex * blockSize;
            if (pos1 != pos2) {
                int tmp = newDNA.get(pos1);
                newDNA.set(pos1, newDNA.get(pos2));
                newDNA.set(pos2, tmp);
            }
        }

        return newDNA;
    }
}
