package me.kb.ga.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DNAScore<DNA> {
    private DNA dna;
    private double score;
}
