package me.kb.ga.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class GAConfig {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Builder.Default
    private int iterationsPerRun = 100;
    @Builder.Default
    private int populationSize = 1000;
    @Builder.Default
    private double copyBestRate = 0.05;
    @Builder.Default
    private double mutationRate = 0.05;
    @Builder.Default
    private double crossoverRate = 0.7;
    @Builder.Default
    private int stagnationGenerations = 50;
    @Builder.Default
    private double stagnationKeepRate = 0.2;
    @Builder.Default
    private double similarityPunishment = 0.1;
    @Builder.Default
    private int similarityCompare = 5;
    @Builder.Default
    private int similaritySkip = 3;
    @Builder.Default
    private double similarityThreshold = 0.85;


    public int getCopyBest() {
        return (int) (copyBestRate * populationSize);
    }

    public int getCrossover() {
        return (int) (crossoverRate * populationSize);
    }

    public int getStagnationKeep() {
        return (int) (stagnationKeepRate * populationSize);
    }

    public boolean isValid() {
        return populationSize > 0 &&
                copyBestRate >= 0 && copyBestRate <= 1 &&
                mutationRate >= 0 && mutationRate <= 1 &&
                crossoverRate >= 0 && crossoverRate <= 1 &&
                stagnationGenerations >= 0 &&
                stagnationKeepRate >= 0 &&  stagnationKeepRate <= 1 &&
                similarityThreshold >= 0 && similarityThreshold <= 1 &&
                similarityPunishment >= 0 &&
                similarityCompare >= 0;
    }


    public JsonNode toJson() {
        return mapper.valueToTree(this);
    }

    public static GAConfig fromJson(JsonNode json) {
        return mapper.convertValue(json, GAConfig.class);
    }
}
