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
public class MutateBlockPermutations implements GAMutate<byte[]> {
    int blockSize;

    @Override
    public byte[] mutate(Random random,byte[] DNA, double rate) {
        byte[] newDNA = new byte[DNA.length];
        System.arraycopy(DNA, 0, newDNA, 0, DNA.length);

        if (DNA.length % blockSize != 0) {
            throw new IllegalArgumentException("DNA size must be divisible by block size");
        }

        int blockCount = DNA.length / blockSize;

        int bound = (int) (DNA.length * rate);
        if (bound <= 0) return newDNA;

        int permutations = random.nextInt(bound);

        for (int i = 0; i < permutations; i++) {
            int blockIndex = random.nextInt(blockCount);
            int pos1 = random.nextInt(blockSize) + blockIndex * blockSize;
            int pos2 = random.nextInt(blockSize) + blockIndex * blockSize;
            if (pos1 != pos2) {
                byte tmp = newDNA[pos1];
                newDNA[pos1] = newDNA[pos2];
                newDNA[pos2] = tmp;
            }
        }

        return newDNA;
    }
}
