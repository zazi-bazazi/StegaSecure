package org.example.model.ga.interfaces;

import java.util.List;

public abstract class IPopulation {
    public abstract void addChromosome(IChromosome chromosome);
    public abstract void addEdge(IChromosome c1, IChromosome c2);
    public abstract List<IChromosome> getNeighbors(IChromosome chromosome);
    public abstract void replaceNode(IChromosome oldNode, IChromosome newNode);
}
