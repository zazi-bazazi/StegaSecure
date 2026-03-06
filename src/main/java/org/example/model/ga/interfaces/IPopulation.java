package org.example.model.ga.interfaces;

import java.util.List;

public interface IPopulation {
    void addChromosome(IChromosome chromosome);
    void addEdge(IChromosome c1, IChromosome c2);
    List<IChromosome> getNeighbors(IChromosome chromosome);
    void replaceNode(IChromosome oldNode, IChromosome newNode);
}
