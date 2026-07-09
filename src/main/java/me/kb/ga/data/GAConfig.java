package me.kb.ga.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GAConfig {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Builder.Default
    private int iterationsPerRun = 1500;
    @Builder.Default
    private int populationSize = 500;
    @Builder.Default
    private double copyBestRate = 0.02;
    @Builder.Default
    private double mutationRate = 0.09;
    @Builder.Default
    private double crossoverRate = 0.75;
    @Builder.Default
    private int stagnationGenerations = 100;
    @Builder.Default
    private double stagnationKeepRate = 0.2;
    @Builder.Default
    private double similarityPunishment = 2.5;
    @Builder.Default
    private int similarityCompare = 30;
    @Builder.Default
    private int similaritySkip = 3;
    @Builder.Default
    private double similarityThreshold = 0.82;
    @Builder.Default
    private int delayBetweenGenerationsMs = 0;
    @Builder.Default
    private int randomSeed = 0;


    public static GAConfig read(File file) throws IOException {
        return mapper.readValue(file, GAConfig.class);
    }

    public void write(File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, this);
    }

    @JsonIgnore
    public int getCopyBest() {
        return (int) (copyBestRate * populationSize);
    }

    @JsonIgnore
    public int getCrossover() {
        return (int) (crossoverRate * populationSize);
    }

    @JsonIgnore
    public int getStagnationKeep() {
        return (int) (stagnationKeepRate * populationSize);
    }

    @JsonIgnore
    public boolean isValid() {
        return populationSize > 0 &&
                copyBestRate >= 0 && copyBestRate <= 1 &&
                mutationRate >= 0 && mutationRate <= 1 &&
                crossoverRate >= 0 && crossoverRate <= 1 &&
                stagnationGenerations >= 0 &&
                stagnationKeepRate >= 0 && stagnationKeepRate <= 1 &&
                similarityThreshold >= 0 && similarityThreshold <= 1 &&
                similarityPunishment >= 0 &&
                similarityCompare >= 0;
    }

}
