package org.example.model.ga.abstractClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractPopulation {
    public abstract void addChromosome(AbstractChromosome<?> chromosome);
    public abstract void addEdge(AbstractChromosome<?> c1, AbstractChromosome<?> c2);
    public abstract List<AbstractChromosome<?>> getNeighbors(AbstractChromosome<?> chromosome);
    public abstract void replaceNode(AbstractChromosome<?> oldNode, AbstractChromosome<?> newNode);
    public abstract ArrayList<AbstractChromosome<?>> getChromosomes();
}
