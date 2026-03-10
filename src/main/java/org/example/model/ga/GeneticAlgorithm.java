package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;
import org.example.model.ga.abstractClasses.AbstractPopulation;

import java.util.function.Function;

public class GeneticAlgorithm {
    private AbstractPopulation population;

    private static final int BLOCK_SIZE = 64;

    public void runGeneration(Function<AbstractChromosome<?>, Double> fitnessFunction) {
        for (Chromosome chromo : population.getChromosomes()) {
            chromo.setFitnessScore(fitnessFunction.apply(chromo));
        }
    }

    public void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> prototype, int popSize, Object... params) {
        this.population = emptyPop;

        for (int i = 0; i < popSize / 2; i++) {
            AbstractChromosome<?> newChromo = prototype.generateChromosomeRand(params);
            this.population.addChromosome(newChromo);
        }

        for (int i = 0; i < popSize / 2; i++) {
            AbstractChromosome<?> newChromo = prototype.generateChromosomeMath(params);
            this.population.addChromosome(newChromo);
        }
    }

    public AbstractPopulation getPopulation() {
        return this.population;
    }

    private AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1, AbstractChromosome<?> chromosome2) {
        AbstractChromosome<?> newChro = chromosome1.createEmpty();
        newChro.crossover(chromosome1, chromosome2);
        return newChro;
    }

    private void mutate(AbstractChromosome<?> chromosome) {
        for (int i = 0; i < chromosome.getDefaultGeneLength(); i++) {
            if (Math.random() <= mutationRate) {
                AbstractGene<?> gene = (byte) Math.round(Math.random());
                chromosome.addGene(gene);
            }
        }
    }

    private AbstractChromosome<?> tournamentSelection(Population pop) {
        Population tournament = new Population(tournamentSize, false);
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.getIndividuals().size());
            tournament.getIndividuals().add(i, pop.getIndividual(randomId));
        }
        return tournament.getFittest();
    }

}
