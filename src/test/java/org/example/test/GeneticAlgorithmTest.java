package org.example.test;

import org.example.model.ga.Chromosome;
import org.example.model.ga.GeneticAlgorithm;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;
import org.example.model.ga.abstractClasses.FitnessFunction;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class GeneticAlgorithmTest {

    @Test
    public void testFullEvolutionLoop() {
        // 1. Create a Dummy Fitness Function: Just add up the block indices!
        // The GA should evolve to find genes with the highest possible block numbers.
        FitnessFunction dummyFitness = (chromosome) -> {
            double score = 0;
            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                AbstractGene<?> gene = chromosome.getGeneByIndex(i);
                // Assuming your Gene class has getBlockIndex()
                score += ((org.example.model.ga.Gene) gene).getBlockIndex();
            }
            return score;
        };

        // 2. Initialize the GA Orchestrator
        int totalBlocks = 100;
        int messageLength = 15;
        GeneticAlgorithm ga = new GeneticAlgorithm(dummyFitness, totalBlocks, messageLength);

        Population emptyPop = new Population();
        Chromosome emptyChro = new Chromosome();

        // 3. Define the Stopping Condition (Stop if a chromosome scores >= 1400)
        Predicate<AbstractChromosome<?>> stopEarly = (c) -> c.getFitnessScore() >= 1400.0;

        // 4. Run the Evolution!
        // This tests the params pipeline (totalBlocks, messageLength)
        AbstractChromosome<?> bestResult = ga.evolve(emptyPop, emptyChro, stopEarly);

        // ASSERTIONS
        assertNotNull(bestResult, "Evolution returned a null chromosome!");

        // Assert the graph didn't explode in size (should remain strictly at populationSize, which is 100)
        assertEquals(200, ga.getPopulation().getChromosomes().size(), "Population size must remain strictly constant!");

        // Assert the GA actually learned and improved the score
        assertTrue(bestResult.getFitnessScore() > 0, "Fitness score did not improve! Evolution failed.");

        System.out.println("Evolution successful! Best dummy score achieved: " + bestResult.getFitnessScore());
    }
}