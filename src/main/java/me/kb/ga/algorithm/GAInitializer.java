package me.kb.ga.algorithm;

import java.util.List;
import java.util.Random;

public interface GAInitializer<DNA> {
    List<DNA> init(Random random, int size);
}
