package me.kb.ga.algorithm.impl.selector;

import me.kb.ga.algorithm.GASelector;
import me.kb.ga.data.DNAScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectorWeightedRandom implements GASelector {
    @Override
    public <DNA> List<DNA> select(Random random, List<DNAScore<DNA>> candidates, int selectCount) {
        List<DNA> selected = new ArrayList<>();

        double totalWeight = candidates.stream().mapToDouble(DNAScore::getScore).sum();


        while (selected.size() < selectCount) {
            double randomWeight = random.nextDouble() * totalWeight;

            double currentWeight = 0;
            for (DNAScore<DNA> entry : candidates) {
                currentWeight += entry.getScore();
                if (currentWeight > randomWeight) {
                    selected.add(entry.getDna());
                    break;
                }
            }
        }

        return selected;
    }
}
