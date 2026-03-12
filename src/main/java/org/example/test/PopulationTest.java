package org.example.test;

import org.example.model.ga.*;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractPopulation;
import org.example.model.stego.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class PopulationTest {

    @Test
    public void testRingTopologyInitialization() {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        AbstractPopulation pop = new Population();
        AbstractChromosome<?> prototype = new StegoChromosome();

        // 1. Initialize a population of exactly 10 chromosomes
        int popSize = 10;
        ga.initializePopulation(pop, prototype, popSize, 5); 

        // 2. Assert population size is strictly maintained
        assertEquals(popSize, pop.getChromosomes().size(), "Population should be exactly 10.");

        // 3. Assert the Ring Topology (Every node should have exactly 2 neighbors)
        for (AbstractChromosome<?> chromo : pop.getChromosomes()) {
            List<AbstractChromosome<?>> neighbors = pop.getNeighbors(chromo);
            assertEquals(2, neighbors.size(), "Every chromosome must have exactly 2 neighbors in a Ring.");
        }
    }

    @Test
    public void testReplaceNodeMaintainsEdges() {
        StegoPopulation pop = new StegoPopulation();
        StegoChromosome parent = new StegoChromosome();
        StegoChromosome neighborA = new StegoChromosome();
        StegoChromosome neighborB = new StegoChromosome();
        
        // Build a micro-graph: A -- Parent -- B
        pop.addChromosome(parent);
        pop.addChromosome(neighborA);
        pop.addChromosome(neighborB);
        pop.addEdge(parent, neighborA);
        pop.addEdge(parent, neighborB);

        // Prove parent has 2 neighbors before replacement
        assertEquals(2, pop.getNeighbors(parent).size());

        // Create a new child to assassinate the parent
        StegoChromosome child = new StegoChromosome();
        pop.replaceNode(parent, child);

        // ASSERTIONS
        // 1. Parent should be completely gone from the graph
        assertEquals(0, pop.getNeighbors(parent).size(), "Parent should have no neighbors after deletion.");
        assertFalse(pop.getChromosomes().contains(parent), "Parent should be removed from the population list.");

        // 2. Child should have inherited exactly 2 neighbors (A and B)
        List<AbstractChromosome<?>> childNeighbors = pop.getNeighbors(child);
        assertEquals(2, childNeighbors.size(), "Child should inherit exactly 2 neighbors.");
        assertTrue(childNeighbors.contains(neighborA));
        assertTrue(childNeighbors.contains(neighborB));

        // 3. Neighbors A and B should now point to Child, not Parent
        assertTrue(pop.getNeighbors(neighborA).contains(child), "Neighbor A must point to Child.");
        assertFalse(pop.getNeighbors(neighborA).contains(parent), "Neighbor A must NOT point to Parent.");
    }
}