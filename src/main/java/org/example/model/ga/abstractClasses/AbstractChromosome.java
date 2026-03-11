package org.example.model.ga.abstractClasses;

import java.util.Collection;

public abstract class AbstractChromosome<T extends Collection<AbstractGene<?>>>  implements Comparable<AbstractChromosome> {
    protected Double fitnessScore;
    protected T chromosomeGenes;

    public Double getFitnessScore() { return this.fitnessScore; }
    public void setFitnessScore(Double score) { this.fitnessScore = score; }
    public int getNumGenes() {return this.chromosomeGenes.size();};

    public abstract T getGenes();
    public abstract AbstractChromosome<T> createEmpty();
    public abstract void addGene(AbstractGene<?> gene);
    public abstract AbstractGene<?> getGeneByIndex(int i);
    public abstract AbstractChromosome<T> generateChromosomeRand(Object... params);
    public abstract AbstractChromosome<T> generateChromosomeMath(Object... params);

    //Algorithm implementation
    public abstract void crossover(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params);
    public abstract void tourmentSelection(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params);
    public abstract void mutate(Object... params);

}
