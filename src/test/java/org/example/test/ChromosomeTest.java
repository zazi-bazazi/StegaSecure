package org.example.test;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ChromosomeTest {

    @Test
    public void testCrossoverPreventsDuplicatesAndShrinking() {
        Chromosome parent1 = new Chromosome();
        Chromosome parent2 = new Chromosome();

        // 1. Force an extreme edge case: Both parents have the EXACT same genes
        // This guarantees that the duplicate safety net and fallback will trigger.
        for (int i = 0; i < 10; i++) {
            parent1.addGene(new Gene(10 + i, 5)); // Block 5, Coeffs 10-19
            parent2.addGene(new Gene(10 + i, 5)); // Exact same!
        }

        Chromosome child = new Chromosome();

        // 2. Run Crossover! Passing totalBlocks = 100 for the fallback generator
        int totalBlocks = 100;
        child.crossover(parent1, parent2, totalBlocks);

        // ASSERTION 1: The child must not shrink! It must have exactly 10 genes.
        assertEquals(10, child.getNumGenes(), "FATAL: Chromosome shrank during crossover! Fallback failed.");

        // ASSERTION 2: Every single gene in the child must be strictly unique.
        Set<Gene> uniqueCheck = new HashSet<>();
        for (int i = 0; i < child.getNumGenes(); i++) {
            Gene g = (Gene) child.getGeneByIndex(i);
            assertTrue(uniqueCheck.add(g), "FATAL: Duplicate gene found at Block " + g.getBlockIndex() + ", Coeff " + g.getCoefficientIndex());
        }

        System.out.println("Chromosome safety nets passed perfectly. Length preserved and duplicates blocked.");
    }
}