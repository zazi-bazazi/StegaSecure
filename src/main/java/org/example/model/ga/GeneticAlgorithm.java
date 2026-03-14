package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;
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
        super(function, 0.1, 10, 200, 200);
        this.totalBlocks = (int) params[0];
        this.messageLength = (int) params[1];
        this.tournamentSize = 5;
    }

    @Override
    public void runGeneration() {
        System.out.println("[INFO] Started " + (new Throwable()).getStackTrace()[0].getMethodName());
        for (AbstractChromosome<?> chromo : this.population.getChromosomes()) {
            chromo.setFitnessScore(this.fitness.evaluateFitness(chromo));
        }
    }

    @Override
    public void initializePopulation(AbstractPopulation emptyPop, AbstractChromosome<?> emptyChro, Object... params) {
        System.out.println("[INFO] Started " + (new Throwable()).getStackTrace()[0].getMethodName());
        this.setPopulation(emptyPop);

//        this.population.addChromosome(emptyChro.generateChromosomeMath(this.totalBlocks, this.messageLength));

        for (int i = 0; i < this.populationSize; i++) {
            AbstractChromosome<?> newChromo = emptyChro.generateChromosomeRand(this.totalBlocks, this.messageLength);
            this.population.addChromosome(newChromo);
        }

        for (int i = 0; i < this.populationSize; i++) {
            AbstractChromosome<?> current = this.population.getChromosomes().get(i);

            for (int step = 1; step <= 3; step++) {
                AbstractChromosome<?> neighbor = this.population.getChromosomes().get((i + step) % this.populationSize);
                this.population.addEdge(current, neighbor);
            }
        }
    }

    @Override
    public void nextGeneration(Object... params) {
        AbstractChromosome<?> parent1 = select();
        AbstractChromosome<?> parent2 = select();

        do {
            parent2 = select();
        } while (parent2 == parent1);

        AbstractChromosome<?> child = this.crossover(parent1, parent2);
        this.mutate(child, this.totalBlocks, this.mutationRate);

        child.setFitnessScore(this.fitness.evaluateFitness(child));

        if (child.compareTo(parent1) > 0) {
            this.population.replaceNode(parent1, child);
        }
    }

    @Override
    protected AbstractChromosome<?> crossover(AbstractChromosome<?> chromosome1, AbstractChromosome<?> chromosome2,
            Object... params) {
        AbstractChromosome<?> newChro = chromosome1.createEmpty();
        newChro.crossover(chromosome1, chromosome2, this.totalBlocks);
        return newChro;
    }

    @Override
    protected void mutate(AbstractChromosome<?> chromosome, Object... params) {
        chromosome.mutate(this.totalBlocks, this.mutationRate);
    }

    // Tourment selection:
    // @Override
    // protected AbstractChromosome<?> select(Object... params) {
    //
    // List<AbstractChromosome<?>> allChromosomes =
    // this.population.getChromosomes();
    // List<AbstractChromosome<?>> family = this.population.getNeighbors(chro);
    //
    // AbstractChromosome<?> bestMate = null;
    //
    // for (int i = 0; i < this.tournamentSize; i++) {
    // AbstractChromosome<?> candidate;
    // do {
    // candidate = allChromosomes.get(random.nextInt(allChromosomes.size()));
    // } while (candidate == chro || family.contains(candidate));
    //
    // if (bestMate == null || candidate.getFitnessScore() >
    // bestMate.getFitnessScore()) {
    // bestMate = candidate;
    // }
    // }
    //
    // return bestMate;
    // }
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

//     @Override
//     protected AbstractChromosome<?> select(Object... params) {
//     List<AbstractChromosome<?>> allChromosomes =
//     this.population.getChromosomes();
//     AbstractChromosome<?> best = null;
//
//     // Pick k random individuals and find the best one
//     for (int i = 0; i < this.tournamentSize; i++) {
//     AbstractChromosome<?> candidate =
//     allChromosomes.get(random.nextInt(allChromosomes.size()));
//
//     if (best == null || candidate.getFitnessScore() > best.getFitnessScore()) {
//     best = candidate;
//     }
//     }
//
//     return best; // Return the winner to act as Parent 1 or Parent 2
//     }
}
