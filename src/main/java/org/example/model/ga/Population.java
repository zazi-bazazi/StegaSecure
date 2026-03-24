package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractPopulation;

import java.util.*;

public class Population extends AbstractPopulation {
    private final Map<AbstractChromosome<?>, List<AbstractChromosome<?>>> adjList;

    public Population() {
        this.adjList = new LinkedHashMap<>();
    }

    /**
     *
     * @param chromosome
     */
    @Override
    public void addChromosome(AbstractChromosome<?> chromosome) {
        this.adjList.putIfAbsent(chromosome, new ArrayList<>());
    }

    /**
     *
     * @param c1
     * @param c2
     */
    @Override
    public void addEdge(AbstractChromosome<?> c1, AbstractChromosome<?> c2) {
        this.adjList.get(c1).add(c2);
        this.adjList.get(c2).add(c1);
    }

    /**
     *
     * @return
     */
    @Override
    public ArrayList<AbstractChromosome<?>> getChromosomes() {
        return new ArrayList<>(this.adjList.keySet());
    }

    /**
     *
     * @param chromosome
     * @return
     */
    @Override
    public List<AbstractChromosome<?>> getNeighbors(AbstractChromosome<?> chromosome) {
        return this.adjList.getOrDefault(chromosome, new ArrayList<>());
    }

    /**
     *
     * @param oldNode
     * @param newNode
     */
    @Override
    public void replaceNode(AbstractChromosome<?> oldNode, AbstractChromosome<?> newNode) {
        if (!this.adjList.containsKey(oldNode)) return;

        // Get the neighbors of the old parent
        List<AbstractChromosome<?>> neighbors = this.adjList.remove(oldNode);

        // Remove the old parent and add the new child
        this.adjList.put(newNode, neighbors);

        // 3. Update every neighbor's own list to point to newNode
        for (AbstractChromosome<?> neighbor : neighbors) {
            List<AbstractChromosome<?>> neighborsOfNeighbor = this.adjList.get(neighbor);
            if (neighborsOfNeighbor != null) {
                // Replace the old reference with the new one
                if (!Collections.replaceAll(neighborsOfNeighbor, oldNode, newNode)) {
                    // todo add new exception for this kind of error
                    throw new IllegalStateException("Graph Integrity Error: Broken bidirectional edge detected. Neighbor did not contain the old node.");
                }
            }
        }
    }
}
