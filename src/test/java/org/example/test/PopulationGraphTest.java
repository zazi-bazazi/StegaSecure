package org.example.test;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PopulationGraphTest {

    @Test
    public void testAddChromosomeAndRetrieve() {
        Population pop = new Population();
        Chromosome c1 = new Chromosome();
        Chromosome c2 = new Chromosome();

        pop.addChromosome(c1);
        pop.addChromosome(c2);

        assertEquals(2, pop.getChromosomes().size());

        System.out.println("SUCCESS: Add and retrieve chromosomes works.");
    }

    @Test
    public void testAddEdgeBidirectional() {
        Population pop = new Population();
        Chromosome c1 = new Chromosome();
        Chromosome c2 = new Chromosome();

        pop.addChromosome(c1);
        pop.addChromosome(c2);
        pop.addEdge(c1, c2);

        assertTrue(pop.getNeighbors(c1).contains(c2), "c1 should see c2 as neighbor.");
        assertTrue(pop.getNeighbors(c2).contains(c1), "c2 should see c1 as neighbor.");

        System.out.println("SUCCESS: Edges are bidirectional.");
    }

    @Test
    public void testReplaceNodePreservesEdges() {
        Population pop = new Population();
        Chromosome c1 = new Chromosome();
        Chromosome c2 = new Chromosome();
        Chromosome c3 = new Chromosome();

        pop.addChromosome(c1);
        pop.addChromosome(c2);
        pop.addChromosome(c3);

        pop.addEdge(c1, c2);
        pop.addEdge(c1, c3);

        // Replace c1 with a new chromosome
        Chromosome replacement = new Chromosome();
        pop.replaceNode(c1, replacement);

        // Old node should be gone
        assertFalse(pop.getChromosomes().contains(c1), "Old node should be removed.");
        assertTrue(pop.getChromosomes().contains(replacement), "Replacement should be in the population.");

        // Replacement should inherit c1's edges
        assertTrue(pop.getNeighbors(replacement).contains(c2), "Replacement should neighbor c2.");
        assertTrue(pop.getNeighbors(replacement).contains(c3), "Replacement should neighbor c3.");

        // c2 and c3 should now point to replacement, not c1
        assertTrue(pop.getNeighbors(c2).contains(replacement), "c2 should point to replacement.");
        assertTrue(pop.getNeighbors(c3).contains(replacement), "c3 should point to replacement.");

        System.out.println("SUCCESS: replaceNode preserves edge structure.");
    }

    @Test
    public void testPopulationSizeConstantAfterReplace() {
        Population pop = new Population();
        for (int i = 0; i < 50; i++) {
            pop.addChromosome(new Chromosome());
        }
        assertEquals(50, pop.getChromosomes().size());

        // Replace the first chromosome
        AbstractChromosome<?> old = pop.getChromosomes().get(0);
        pop.replaceNode(old, new Chromosome());

        assertEquals(50, pop.getChromosomes().size(),
                "Population size must stay constant after replaceNode.");

        System.out.println("SUCCESS: Population size is constant after replacements.");
    }

    @Test
    public void testChromosomeGenerationUniqueness() {
        Chromosome empty = new Chromosome();
        AbstractChromosome<?> generated = empty.generateChromosomeRand(100, 50);

        assertEquals(50, generated.getNumGenes(), "Generated chromosome should have exactly messageLength genes.");

        // All genes should be unique
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < generated.getNumGenes(); i++) {
            Gene g = (Gene) generated.getGeneByIndex(i);
            String key = g.getBlockIndex() + ":" + g.getCoefficientIndex();
            assertTrue(seen.add(key), "Duplicate gene found: " + key);
        }

        System.out.println("SUCCESS: Random chromosome generation produces unique genes.");
    }

    @Test
    public void testChromosomeMathGeneration() {
        Chromosome empty = new Chromosome();
        AbstractChromosome<?> generated = empty.generateChromosomeMath(100, 30);

        assertEquals(30, generated.getNumGenes(), "Math-generated chromosome should have messageLength genes.");

        // All genes should have coefficient indices in [1, 15]
        for (int i = 0; i < generated.getNumGenes(); i++) {
            Gene g = (Gene) generated.getGeneByIndex(i);
            assertTrue(g.getCoefficientIndex() >= 1 && g.getCoefficientIndex() <= 15,
                    "Coefficient index out of range: " + g.getCoefficientIndex());
            assertTrue(g.getBlockIndex() >= 0 && g.getBlockIndex() < 100,
                    "Block index out of range: " + g.getBlockIndex());
        }

        System.out.println("SUCCESS: Math-generated chromosome has valid gene ranges.");
    }

    @Test
    public void testMutationPreservesGeneCount() {
        Chromosome chro = new Chromosome();
        for (int i = 0; i < 20; i++) {
            chro.addGene(new Gene(1 + (i % 15), i));
        }

        int originalSize = chro.getNumGenes();
        chro.mutate(100, 0.5); // 50% mutation rate

        assertEquals(originalSize, chro.getNumGenes(),
                "Mutation should not change the number of genes.");

        System.out.println("SUCCESS: Mutation preserves gene count.");
    }
}
