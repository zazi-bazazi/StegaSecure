package org.example.model.ga;

import org.example.model.ga.interfaces.IChromosome;
import org.example.model.ga.interfaces.IPopulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Population extends IPopulation {
    private final Map<IChromosome, List<IChromosome>> adjList;

    public Population() {
        this.adjList = new HashMap<>();
    }

    @Override
    public void addChromosome(IChromosome chromosome) {
        this.adjList.putIfAbsent(chromosome, new ArrayList<>());
    }

    @Override
    public void addEdge(IChromosome c1, IChromosome c2) {
        this.adjList.get(c1).add(c2);
        this.adjList.get(c2).add(c1);
    }

    @Override
    public List<IChromosome> getNeighbors(IChromosome chromosome) {
        return this.adjList.getOrDefault(chromosome, new ArrayList<>());
    }

    @Override
    public void replaceNode(IChromosome oldNode, IChromosome newNode) {
        if (!this.adjList.containsKey(oldNode)) return;

        // Get the neighbors of the old parent
        List<IChromosome> neighbors = this.adjList.get(oldNode);

        // Remove the old parent and add the new child
        this.adjList.remove(oldNode);
        this.adjList.put(newNode, neighbors);

        // Update the neighbors to point to the new child instead of the old parent
        for (IChromosome neighbor : neighbors) {
            List<IChromosome> neighborList = this.adjList.get(neighbor);
            neighborList.remove(oldNode);
            neighborList.add(newNode);
        }
    }
}
