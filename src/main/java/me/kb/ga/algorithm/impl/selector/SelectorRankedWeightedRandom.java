package me.kb.ga.algorithm.impl.selector;

import lombok.RequiredArgsConstructor;
import me.kb.ga.algorithm.GASelector;
import me.kb.ga.data.DNAScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class SelectorRankedWeightedRandom implements GASelector {
    private final BiFunction<Integer, Integer, Double> rankToWeight; // rank, total count -> weight

    public SelectorRankedWeightedRandom() {
        this((r, count) -> (double) (count - r));
    }


    @Override
    public <DNA> List<DNA> select(Random random, List<DNAScore<DNA>> candidates, int selectCount) {
        List<DNA> selected = new ArrayList<>();


        List<DNAScore<DNA>> ranked = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            ranked.add(new DNAScore<>(candidates.get(i).getDna(), rankToWeight.apply(i, candidates.size())));
        }


        double totalWeight = ranked.stream().mapToDouble(DNAScore::getScore).sum();


        while (selected.size() < selectCount) {
            double randomWeight = random.nextDouble() * totalWeight;

            double currentWeight = 0;
            for (DNAScore<DNA> entry : ranked) {
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
