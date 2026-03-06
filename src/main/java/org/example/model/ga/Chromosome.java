package org.example.model.ga;

import org.example.model.ga.interfaces.IChromosome;
import org.example.model.ga.interfaces.IGene;

import java.util.ArrayList;
import java.util.List;

public class Chromosome implements IChromosome {
    private final ArrayList<IGene> chromosomeGenes;
    private Double fitnessScore;

    public Chromosome() {
        this.chromosomeGenes = new ArrayList<>();
        this.fitnessScore = 0.0;
    }

    public void addGene(IGene gene) {
        this.chromosomeGenes.add(gene);
    }

    @Override
    public List<IGene> getGenes() {
        return this.chromosomeGenes;
    }

    @Override
    public Double getFitnessScore() {
        return this.fitnessScore;
    }

    @Override
    public void setFitnessScore(Double score) {
        this.fitnessScore = score;
    }

    @Override
    public int compareTo(IChromosome o) {
        return Double.compare(this.fitnessScore, o.getFitnessScore());
    }
}

