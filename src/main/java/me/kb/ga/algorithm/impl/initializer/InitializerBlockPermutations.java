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
public class InitializerBlockPermutations implements GAInitializer<byte[]> {
    private int blockCount;
    private int blockSize;

    @Override
    public List<byte[]> init(Random random, int size) {

        List<byte[]> generation = new ArrayList<>();


        for (int i = 0; i < size; i++) {
            byte[] dna = new byte[blockSize * blockCount];

            for (int j = 0; j < blockCount; j++) {
                List<Integer> block = new ArrayList<>();
                for (int s = 1; s <= blockSize; s++) {
                    block.add(s);
                }
                Collections.shuffle(block, random);

                for (int s = 0; s < blockSize; s++) {
                    dna[j * blockSize + s] = block.get(s).byteValue();
                }
            }
            generation.add(dna);
        }

        return generation;
    }
}
