package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class Chromosome extends AbstractChromosome<ArrayList<AbstractGene<?>>> {
    private final Random random = new Random();

    public Chromosome() {
        this.chromosomeGenes = new ArrayList<>();
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> createEmpty() {
        return new Chromosome();
    }

    @Override
    public void addGene(AbstractGene<?> gene) {
        this.chromosomeGenes.add(gene);
    }

    @Override
    public ArrayList<AbstractGene<?>> getGenes() {
        return this.chromosomeGenes;
    }

    @Override
    public int compareTo(AbstractChromosome<?> o) {
        return Double.compare(this.fitnessScore, o.getFitnessScore());
    }

    @Override
    public AbstractGene<?> getGeneByIndex(int i) {
        return this.chromosomeGenes.get(i);
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeRand(Object... params) {
        if (params.length != 2) {
            // TODO make a custom Exception for this state
            throw new IllegalStateException("MISSING PARAMS");
        }

        int totalBlocks = (int) params[0];
        int messageLength = (int) params[1];

        LinkedHashSet<AbstractGene<?>> uniqueGenes = new LinkedHashSet<>();

        while (uniqueGenes.size() < messageLength) {
            int randomBlock = (int) (Math.random() * totalBlocks);
            int randomCoeff = 1 + (int) (Math.random() * 15); // AC indices 1-15 (survive quantization)

            uniqueGenes.add(new Gene(randomCoeff, randomBlock));
        }
        Chromosome newChromo = new Chromosome();
        newChromo.chromosomeGenes.addAll(uniqueGenes);

        return newChromo;
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeMath(Object... params) {
        if (params.length != 2) {
            // TODO make a custom Exception for this state
            throw new IllegalStateException("MISSING PARAMS");
        }

        int totalBlocks = (int) params[0];
        int L = (int) params[1]; // messageLength

        final int usableCoefficientsPerBlock = 15;
        final int N = totalBlocks * usableCoefficientsPerBlock;

        final int NL = (int) Math.floor((double) N / L);

        List<AbstractGene<?>> generatedGenes = new ArrayList<>();

        for (int i = 0; i < L; i++) {

            int pos = (i * NL) % N;

            int blockIndex = pos / usableCoefficientsPerBlock;
            int coeffIndex = 1 + (pos % usableCoefficientsPerBlock);

            generatedGenes.add(new Gene(coeffIndex, blockIndex));
        }

        Chromosome newChromo = new Chromosome();
        for (AbstractGene<?> gene : generatedGenes) {
            newChromo.addGene(gene);
        }

        return newChromo;
    }

    @Override
    public void crossover(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params) {
        for (int i = 0; i < chro1.getNumGenes(); i++) {
            Gene copy1 = new Gene(chro1.getGeneByIndex(i));
            Gene copy2 = new Gene(chro2.getGeneByIndex(i));

            Gene chosenGene = (Math.random() < 0.5) ? copy1 : copy2;
            Gene backupGene = (chosenGene == copy1) ? copy2 : copy1;

            if (!this.getGenes().contains(chosenGene)) {
                this.addGene(chosenGene);
            } else if (!this.getGenes().contains(backupGene)) {
                this.addGene(backupGene);
            } else {
                int totalBlocks = (int) params[0];
                Gene emergencyGene = new Gene(1 + random.nextInt(15), random.nextInt(totalBlocks));

                while (this.getGenes().contains(emergencyGene)) {
                    emergencyGene = new Gene(1 + random.nextInt(15), random.nextInt(totalBlocks));
                }
                this.addGene(emergencyGene);
            }
        }
    }

    @Override
    public void tourmentSelection(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params) {

    }

    @Override
    public void mutate(Object... params) {
        int totalBlocks = (int) params[0];
        double mutationRate = (double) params[1];

        for (int i = 0; i < this.getNumGenes(); i++) {
            Gene currentGene = (Gene) this.getGeneByIndex(i);

            if (random.nextDouble() >= mutationRate)
                continue;

            Gene candidate;
            int attempts = 0;
            do {
                int newBlock = random.nextInt(totalBlocks);
                int newCoeff = 1 + random.nextInt(15);
                candidate = new Gene(newCoeff, newBlock);
                attempts++;

                if (attempts > 50) {
                    candidate = currentGene;
                    break;
                }
            } while (this.getGenes().contains(candidate));

            currentGene.setBlockIndex(candidate.getBlockIndex());
            currentGene.setValue(candidate.getCoefficientIndex());
        }
    }

    @Override
    public String toString() {
        return "Chromosome{genes=" + chromosomeGenes.size() + ", fitness=" + String.format("%.4f", fitnessScore)
                + " Gene= " + this.chromosomeGenes + "}";
    }
}
