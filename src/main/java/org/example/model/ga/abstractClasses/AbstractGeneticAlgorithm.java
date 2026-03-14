package org.example.model.ga.abstractClasses;

import java.util.Random;
import java.util.function.Predicate;

public abstract class AbstractGeneticAlgorithm {
    protected double mutationRate;
    protected double crossoverRate;
    protected int populationSize;
    protected int maxGenerations;
    protected AbstractPopulation population;
    protected final FitnessFunction fitness;
    protected AbstractChromosome<?> bestChromosome = null;

    protected final Random random = new Random();

    // Stagnation restart parameters
    protected static final int STAGNATION_LIMIT = 20;
    protected static final double STAGNATION_TOLERANCE = 0.01;

    public AbstractGeneticAlgorithm(FitnessFunction function, double mutationRate, double crossoverRate,
            int populationSize, int maxGenerations) {
        this.fitness = function;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
    }

    public AbstractChromosome<?> evolve(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro,
            Predicate<AbstractChromosome<?>> stoppingCondition) {

        // Generation 0
        initializePopulation(emptyPop, emptyChro);
        System.out.println("Finished initializing population, grading them next");
        runGeneration();

        double previousAvgFitness = Double.NEGATIVE_INFINITY;
        int stagnationCounter = 0;

        System.out.println("Started evolving");
        for (int generation = 0; generation < this.maxGenerations; generation++) {

            double sumFitness = 0.0;
            for (int i = 0; i < this.populationSize; i++) {
                nextGeneration();
            }
            System.out.println("[INFO] Finished generation number- " + generation);
            for (int i = 0; i < this.populationSize; i++) {
                sumFitness += this.population.getChromosomes().get(i).getFitnessScore();
            }
            double avgFitness = sumFitness / this.populationSize;
            System.out.println("[INFO] Average fitness for generation number: " + generation + " is " + avgFitness);

            // --- Stagnation detection ---
            if (Math.abs(avgFitness - previousAvgFitness) < STAGNATION_TOLERANCE) {
                stagnationCounter++;
            } else {
                stagnationCounter = 0;
            }
            previousAvgFitness = avgFitness;

            if (stagnationCounter >= STAGNATION_LIMIT) {
                System.out.println("[RESTART] Stagnation detected at generation " + generation
                        + " — restarting bottom 50% of population.");
                restartPopulation(emptyChro);
                stagnationCounter = 0;
            }

            AbstractChromosome<?> currentBest = getBestChromosome();

            if (bestChromosome == null || currentBest.getFitnessScore() > bestChromosome.getFitnessScore()) {
                bestChromosome = currentBest;
            }

            if (stoppingCondition.test(currentBest)) {
                System.out.println("Evolution finished early at generation: " + generation);
                break;
            }
        }

        return bestChromosome;
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

    public AbstractPopulation getPopulation() {
        return this.population;
    }

    protected void setPopulation(AbstractPopulation pop) {
        this.population = pop;
    }

    public abstract void runGeneration();

    public abstract void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro,
            Object... params);

    public abstract void nextGeneration(Object... params);

    protected abstract AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1,
            AbstractChromosome<?> chromosome2, Object... params);

    protected abstract void mutate(AbstractChromosome<?> chromosome, Object... params);

    protected abstract AbstractChromosome<?> select(Object... params);

    protected abstract void restartPopulation(AbstractChromosome<?> emptyChro);

}
