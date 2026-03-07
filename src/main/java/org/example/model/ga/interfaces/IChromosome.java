package org.example.model.ga.interfaces;

import java.util.List;

public interface IChromosome extends Comparable<IChromosome> {
    void addGene(IGene gene);
    List<IGene> getGenes();
    Double getFitnessScore();
    void setFitnessScore(Double score);
}
