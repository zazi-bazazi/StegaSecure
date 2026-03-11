package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGeneticAlgorithm;
import org.example.model.ga.abstractClasses.AbstractPopulation;
import org.example.model.ga.abstractClasses.FitnessFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GeneticAlgorithm extends AbstractGeneticAlgorithm {

    private static final int BLOCK_SIZE = 64;

    private final int totalBlocks;
    private final int messageLength;
    private final int tournamentSize;

    public GeneticAlgorithm(FitnessFunction function, Object... params) {
        super(function, 0.1, 10 ,100);
        this.totalBlocks = (int) params[0];
        this.messageLength = (int)params[1];
        this.tournamentSize = 3;
    }

    @Override
    public void runGeneration() {
        for (AbstractChromosome<?> chromo : this.population.getChromosomes()) {
            chromo.setFitnessScore(this.fitness.evaluateFitness(chromo));
        }
    }

    @Override
    public void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro, Object... params) {
        this.setPopulation(emptyPop);

        for (int i = 0; i < this.populationSize / 2; i++) {
            AbstractChromosome<?> newChromo = emptyChro.generateChromosomeRand(params);
            this.population.addChromosome(newChromo);
        }

        for (int i = 0; i < this.populationSize / 2; i++) {
            AbstractChromosome<?> newChromo = emptyChro.generateChromosomeMath(params);
            this.population.addChromosome(newChromo);
        }
    }



    @Override
    public void nextGeneration(Object... params) {
        List<AbstractChromosome<?>> allNodes = new ArrayList<>(this.population.getChromosomes());
        AbstractChromosome<?> parent1 = allNodes.get(random.nextInt(allNodes.size()));

        // 2. Select Parent 2 using Local Tournament (e.g., tournament size of 3)
        AbstractChromosome<?> parent2 = select(parent1);

        // 3. Delegate Crossover to the Chromosome
        AbstractChromosome<?> child = this.crossover(parent1, parent2);

        // 4. Delegate Mutation (Passing the required environment variable)
        this.mutate(child, this.totalBlocks, this.mutationRate);

        // 5. Evaluate the new child's fitness
         child.setFitnessScore(this.fitness.evaluateFitness(child));

        // 6. Replacement Strategy
        // If the new child is better than Parent 1, it takes Parent 1's spot in the graph!
        if (child.compareTo(parent1) > 0) {
            this.population.replaceNode(parent1, child);
        }
    }

    @Override
    protected AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1, AbstractChromosome<?> chromosome2, Object... params) {
        AbstractChromosome<?> newChro = chromosome1.createEmpty();
        newChro.crossover(chromosome1, chromosome2);
        return newChro;
    }

    @Override
    protected void mutate(AbstractChromosome<?> chromosome, Object... params) {
        chromosome.mutate(this.totalBlocks, this.mutationRate);
    }

    @Override
    protected AbstractChromosome<?> select(AbstractChromosome<?> chro, Object... params) {
        List<AbstractChromosome<?>> neighbors = this.population.getNeighbors(chro);

        if (neighbors == null || neighbors.isEmpty()) {
            return chro;
        }

        AbstractChromosome<?> bestMate = null;

        for (int i = 0; i < this.tournamentSize; i++) {
            AbstractChromosome<?> randomNeighbor = neighbors.get(random.nextInt(neighbors.size()));

            if (bestMate == null || randomNeighbor.getFitnessScore() > bestMate.getFitnessScore()) {
                bestMate = randomNeighbor;
            }
        }
        return bestMate;
    }
}
