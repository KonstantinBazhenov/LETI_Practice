package me.kb.ga.algorithm;

import java.util.Random;

public interface GAMutate<DNA> {
    DNA mutate(Random random, DNA instance, double rate);
}
