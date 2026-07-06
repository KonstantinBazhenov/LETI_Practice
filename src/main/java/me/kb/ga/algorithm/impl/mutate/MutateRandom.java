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
public class MutateRandom implements GAMutate<List<Integer>> {
    private int rangeMin;
    private int rangeMax; // не включительно

    @Override
    public List<Integer> mutate(Random random, List<Integer> DNA, double rate) {
        List<Integer> newDNA = new ArrayList<>(DNA);


        for (int i = 0; i < newDNA.size(); i++) {
            if (random.nextDouble() < rate) {
                newDNA.set(i, random.nextInt(rangeMin, rangeMax));
            }
        }

        return newDNA;
    }
}
