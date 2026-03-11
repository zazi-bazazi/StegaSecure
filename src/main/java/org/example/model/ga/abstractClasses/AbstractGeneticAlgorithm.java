package org.example.model.ga.abstractClasses;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractGeneticAlgorithm{
    protected double mutationRate;
    protected double crossoverRate;
    protected int populationSize;
    protected int maxGenerations;
    protected AbstractPopulation population;
    protected final FitnessFunction fitness;

    protected final Random random = new Random();

    public AbstractGeneticAlgorithm(FitnessFunction function, double mutationRate, double crossoverRate, int populationSize) {
        this.fitness = function;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.populationSize = populationSize;
    }

    public AbstractChromosome<?> evolve(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro, Predicate<AbstractChromosome<?>> stoppingCondition) {

        //Generation 0
        initializePopulation(emptyPop, emptyChro);
        runGeneration();

        // 2. The Evolution Loop (Now safely hidden inside the GA)
        for (int generation = 0; generation < this.maxGenerations; generation++) {

            // Run one step of evolution
            nextGeneration();

            // Find the best chromosome currently in the graph
            AbstractChromosome<?> currentBest = getBestChromosome();

            // 3. The Dynamic Stop Check
            // Ask the Engine's predicate: "Is this chromosome good enough to stop?"
            if (stoppingCondition.test(currentBest)) {
                System.out.println("Evolution finished early at generation: " + generation);
                break;
            }
        }

        // Return the ultimate winner back to the Engine
        return getBestChromosome();
    }

    // Helper method to scan your adjList and find the highest score
    protected AbstractChromosome<?> getBestChromosome() {
        AbstractChromosome<?> best = null;
        for (AbstractChromosome<?> chromo : population.getChromosomes()) {
            if (best == null || chromo.getFitnessScore() > best.getFitnessScore()) {
                best = chromo;
            }
        }
        return best;
    }

    public AbstractPopulation getPopulation() {return this.population;}
    protected void setPopulation(AbstractPopulation pop) {this.population = pop;}

    public abstract void runGeneration();
    public abstract void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro, Object... params);
    public abstract void nextGeneration(Object... params);

    protected abstract AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1, AbstractChromosome<?> chromosome2, Object... params);
    protected abstract void mutate(AbstractChromosome<?> chromosome, Object... params);
    protected abstract AbstractChromosome<?> select(AbstractChromosome<?> focalNode, Object... params);

}
