package me.kb.ga.algorithm.impl.crossover;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.kb.ga.algorithm.GACrossover;

import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class CrossoverBlocksUniform implements GACrossover<byte[]> {
    int blockSize;


    @Override
    public List<byte[]> crossover(Random random, byte[] dna1, byte[] dna2) {

        if (dna1.length != dna2.length) {
            throw new IllegalArgumentException("DNA size mismatch");
        }

        if (dna1.length % blockSize != 0) {
            throw new IllegalArgumentException("DNA size must be divisible by block size");
        }

        int blockCount = dna1.length / blockSize;


        byte[] newDna1 = new byte[dna1.length];
        byte[] newDna2 = new byte[dna1.length];




        for (int i = 0; i < blockCount; i++) {
            if (random.nextBoolean()) {
                for (int j = 0; j < blockSize; j++) {
                    int idx = i * blockSize + j;

                    newDna1[idx] = dna1[idx];
                    newDna2[idx] = dna2[idx];
                }
            } else {
                for (int j = 0; j < blockSize; j++) {
                    int idx = i * blockSize + j;

                    newDna1[idx] = dna2[idx];
                    newDna2[idx] = dna1[idx];
                }
            }
        }


        return List.of(newDna1, newDna2);
    }
}
