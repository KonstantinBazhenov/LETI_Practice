package me.kb.ga.algorithm;

import me.kb.ga.data.DNAScore;

import java.util.List;
import java.util.Random;

public interface GASelector {
    <DNA> List<DNA> select(Random random, List<DNAScore<DNA>> candidates, int selectCount);
}
