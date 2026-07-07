package me.kb.ga.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class RunResult<DNA> {
    DNAScore<DNA> best;
    List<DNAScore<DNA>> bestPerGeneration;
    List<List<DNAScore<DNA>>> generations;
}
