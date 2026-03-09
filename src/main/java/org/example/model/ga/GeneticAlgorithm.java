package org.example.model.ga;

import org.example.model.ga.interfaces.IChromosome;

import java.util.function.Function;

public class GeneticAlgorithm {
    private Population population;

    public void runGeneration(Function<Chromosome, Double> fitnessFunction) {
        for (Chromosome chromo : population.getChromosomes()) {
            chromo.setFitnessScore(fitnessFunction.apply(chromo));
        }
    }

    private IChromosome crossover(IChromosome chromosome1, IChromosome chromosome2) {
        IChromosome newChro = chromosome1.createEmpty();
        for (int i = 0; i < newChro.getDefaultGeneLength(); i++) {
            if (Math.random() <= uniformRate) {
                newChro.setSingleGene(i, chromosome1.getSingleGene(i));
            } else {
                newChro.setSingleGene(i, chromosome2.getSingleGene(i));
            }
        }
        return newChro;
    }

    private void mutate(IChromosome chromosome) {
        for (int i = 0; i < chromosome.getDefaultGeneLength(); i++) {
            if (Math.random() <= mutationRate) {
                byte gene = (byte) Math.round(Math.random());
                chromosome.setSingleGene(i, gene);
            }
        }
    }

    private IChromosome tournamentSelection(Population pop) {
        Population tournament = new Population(tournamentSize, false);
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.getIndividuals().size());
            tournament.getIndividuals().add(i, pop.getIndividual(randomId));
        }
        IChromosome fittest = tournament.getFittest();
        return fittest;
    }

}
