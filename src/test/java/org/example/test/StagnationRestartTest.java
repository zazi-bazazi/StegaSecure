package org.example.test;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.example.model.ga.GeneticAlgorithm;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;
import org.example.model.ga.abstractClasses.FitnessFunction;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class StagnationRestartTest {

    @Test
    public void testStagnationTriggersRestart() {
        // Fitness function that always returns a constant — guarantees stagnation
        FitnessFunction flatFitness = (chromosome) -> 50.0;

        int totalBlocks = 100;
        int messageLength = 10;
        GeneticAlgorithm ga = new GeneticAlgorithm(flatFitness, totalBlocks, messageLength);

        Population emptyPop = new Population();
        Chromosome emptyChro = new Chromosome();

        // Stop condition that will never trigger (fitness is always 50)
        Predicate<AbstractChromosome<?>> neverStop = (c) -> false;

        // This should run all 200 generations. Stagnation should trigger
        // and restart the population (logged to console), but not crash.
        AbstractChromosome<?> best = ga.evolve(emptyPop, emptyChro, neverStop);

        assertNotNull(best, "Evolution must return a non-null chromosome.");
        assertEquals(200, ga.getPopulation().getChromosomes().size(),
                "Population size must remain constant after restarts.");

        System.out.println("SUCCESS: Stagnation restart completed without crash. Population stable.");
    }

    @Test
    public void testPopulationSizePreservedAfterRestart() {
        // Fitness based on block indices — simple but varied
        FitnessFunction variedFitness = (chromosome) -> {
            double score = 0;
            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                Gene gene = (Gene) chromosome.getGeneByIndex(i);
                score += gene.getBlockIndex();
            }
            return score;
        };

        int totalBlocks = 50;
        int messageLength = 8;
        GeneticAlgorithm ga = new GeneticAlgorithm(variedFitness, totalBlocks, messageLength);

        Population emptyPop = new Population();
        Chromosome emptyChro = new Chromosome();

        // Stop early so this test doesn't take forever
        Predicate<AbstractChromosome<?>> stopAt = (c) -> c.getFitnessScore() >= 300.0;

        AbstractChromosome<?> best = ga.evolve(emptyPop, emptyChro, stopAt);

        assertNotNull(best);
        assertEquals(200, ga.getPopulation().getChromosomes().size(),
                "Population size must remain strictly constant throughout evolution.");

        System.out.println("SUCCESS: Population size preserved. Best score: " + best.getFitnessScore());
    }
}
