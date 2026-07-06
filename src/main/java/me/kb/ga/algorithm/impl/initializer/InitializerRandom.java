package me.kb.ga.algorithm.impl.initializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.kb.ga.algorithm.GAInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class InitializerRandom implements GAInitializer<List<Integer>> {
    private int dnaSize;
    private int rangeMin;
    private int rangeMax; // не включительно

    @Override
    public List<List<Integer>> init(Random random, int size) {

        List<List<Integer>> generation = new ArrayList<>();


        for (int i = 0; i < size; i++) {
            List<Integer> dna = new ArrayList<>();

            for (int j = 0; j < dnaSize; j++) {
                dna.add(random.nextInt(rangeMin, rangeMax));
            }
            generation.add(dna);
        }

        return generation;
    }
}
