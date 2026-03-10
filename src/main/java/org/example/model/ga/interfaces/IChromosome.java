package org.example.model.ga.interfaces;

import java.util.List;

public abstract class IChromosome implements Comparable<IChromosome> {
    public abstract IChromosome createEmpty();
    public abstract void addGene(IGene gene);
    public abstract List<IGene> getGenes();
    public abstract Double getFitnessScore();
    public abstract void setFitnessScore(Double score);
}
