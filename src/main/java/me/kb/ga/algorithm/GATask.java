package me.kb.ga.algorithm;

import me.kb.ga.data.DNAScore;

public interface GATask<DNA> {
    double eval(DNA dna);

    boolean shouldStop(DNAScore<DNA> best);

    boolean isCorrect(DNA dna);

    DNA tryCorrect(DNA dna);

    double getSimilarity(DNA dna1, DNA dna2); // Значение в диапазоне 0-1
}
