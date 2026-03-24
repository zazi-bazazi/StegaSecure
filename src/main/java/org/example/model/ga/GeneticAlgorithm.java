package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGeneticAlgorithm;
import org.example.model.ga.abstractClasses.AbstractPopulation;
import org.example.model.ga.abstractClasses.FitnessFunction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class GeneticAlgorithm extends AbstractGeneticAlgorithm {

    private static final int BLOCK_SIZE = 64;

    private final int totalBlocks;
    private final int messageLength;
    private final int tournamentSize;
    private final List<int[]> validPositions; // non-zero (block, coeff) pairs

    @SuppressWarnings("unchecked")
    public GeneticAlgorithm(FitnessFunction function, Object... params) {
        super(function, 0.1, 10, 200, 200);
        this.totalBlocks = (int) params[0];
        this.messageLength = (int) params[1];
        this.validPositions = (params.length > 2) ? (List<int[]>) params[2] : null;
        this.tournamentSize = 5;
    }

    /**
     *
     */
    @Override
    public void runGeneration() {
        System.out.println("[INFO] Started " + (new Throwable()).getStackTrace()[0].getMethodName());
        for (AbstractChromosome<?> chromo : this.population.getChromosomes()) {
            chromo.setFitnessScore(this.fitness.evaluateFitness(chromo));
        }
    }

    /**
     *
     * @param emptyPop
     * @param emptyChro
     * @param params
     */
    @Override
    public void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro, Object... params) {
        System.out.println("[INFO] Started " + (new Throwable()).getStackTrace()[0].getMethodName());
        this.setPopulation(emptyPop);

        for (int i = 0; i < this.populationSize; i++) {
            AbstractChromosome<?> newChromo = emptyChro.generateChromosomeRand(this.totalBlocks, this.messageLength, this.validPositions);
            this.population.addChromosome(newChromo);
        }

        this.savePopulationToFile("D:\\OneDrive\\תמונות\\PopulationFile.txt");

        for (int i = 0; i < this.populationSize; i++) {
            AbstractChromosome<?> current = this.population.getChromosomes().get(i);

            for (int step = 1; step <= 3; step++) {
                AbstractChromosome<?> neighbor = this.population.getChromosomes().get((i + step) % this.populationSize);
                this.population.addEdge(current, neighbor);
            }
        }
    }

    /**
     *
     * @param params
     */
    @Override
    public void nextGeneration(Object... params) {
        AbstractChromosome<?> parent1 = select();
        AbstractChromosome<?> parent2 = select();

        do {
            parent2 = select();
        } while (parent2 == parent1);

        AbstractChromosome<?> child = this.crossover(parent1, parent2);
        this.mutate(child, this.totalBlocks, this.mutationRate, this.validPositions);

        child.setFitnessScore(this.fitness.evaluateFitness(child));

        if (child.compareTo(parent1) > 0) {
            this.population.replaceNode(parent1, child);
        }
    }

    /**
     *
     * @param chromosome1
     * @param chromosome2
     * @param params
     * @return
     */
    @Override
    protected AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1, AbstractChromosome<?> chromosome2,
            Object... params) {
        AbstractChromosome<?> newChro = chromosome1.createEmpty();
        newChro.crossover(chromosome1, chromosome2, this.totalBlocks);
        return newChro;
    }

    /**
     *
     * @param chromosome
     * @param params
     */
    @Override
    protected void mutate(AbstractChromosome<?> chromosome, Object... params) {
        chromosome.mutate(this.totalBlocks, this.mutationRate, validPositions);
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    protected AbstractChromosome<?> select(Object... params) {
        List<AbstractChromosome<?>> allChromosomes = this.population.getChromosomes();

        AbstractChromosome<?> anchor = allChromosomes.get(random.nextInt(allChromosomes.size()));

        List<AbstractChromosome<?>> neighborhood = new ArrayList<>(this.population.getNeighbors(anchor));
        neighborhood.add(anchor);

        AbstractChromosome<?> best = null;
        int k = Math.min(this.tournamentSize, neighborhood.size());
        for (int i = 0; i < k; i++) {
            AbstractChromosome<?> candidate = neighborhood.get(random.nextInt(neighborhood.size()));
            if (best == null || candidate.getFitnessScore() > best.getFitnessScore()) {
                best = candidate;
            }
        }
        return best;
    }

    // @Override
    // protected AbstractChromosome<?> select(Object... params) {
    // List<AbstractChromosome<?>> allChromosomes =
    // this.population.getChromosomes();
    // AbstractChromosome<?> best = null;
    //
    // // Pick k random individuals and find the best one
    // for (int i = 0; i < this.tournamentSize; i++) {
    // AbstractChromosome<?> candidate =
    // allChromosomes.get(random.nextInt(allChromosomes.size()));
    //
    // if (best == null || candidate.getFitnessScore() > best.getFitnessScore()) {
    // best = candidate;
    // }
    // }
    //
    // return best; // Return the winner to act as Parent 1 or Parent 2
    // }

    /**
     *
     * @param emptyChro
     */
    @Override
    protected void restartPopulation(AbstractChromosome<?> emptyChro) {
        List<AbstractChromosome<?>> all = this.population.getChromosomes();

        // Sort ascending by fitness so the weakest are at the front
        all.sort(Comparator.comparingDouble(AbstractChromosome::getFitnessScore));

        int replaceCount = all.size() / 2;

        for (int i = 0; i < replaceCount; i++) {
            AbstractChromosome<?> weakest = all.get(i);

            // Generate a fresh random chromosome using valid positions
            AbstractChromosome<?> fresh = emptyChro.generateChromosomeRand(this.totalBlocks, this.messageLength, this.validPositions);
            fresh.setFitnessScore(this.fitness.evaluateFitness(fresh));

            // Swap it into the graph, inheriting the old one's edges
            this.population.replaceNode(weakest, fresh);
        }

        System.out.println("[RESTART] Replaced " + replaceCount + " weakest chromosomes with fresh random ones.");
    }
}
