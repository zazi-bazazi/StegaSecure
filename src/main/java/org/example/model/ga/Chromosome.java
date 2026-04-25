package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;

import java.util.*;

public class Chromosome extends AbstractChromosome<ArrayList<AbstractGene<?>>> {
    private final Random random = new Random();

    private static final int USEABLE_COEFF_PER_BLOCK = 19;
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

    /**
     *
     * @param params
     * @return
     */
    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeRand(Object... params) {
        if (params.length < 2) {
            throw new IllegalStateException("MISSING PARAMS");
        }

        int totalBlocks = (int) params[0];
        int messageLength = (int) params[1];
        List<int[]> validPositions = (params.length > 2 && params[2] != null) ? (List<int[]>) params[2] : null;

        LinkedHashSet<AbstractGene<?>> uniqueGenes = new LinkedHashSet<>();

        while (uniqueGenes.size() < messageLength) {
            uniqueGenes.add(getRandomGene(validPositions, totalBlocks));
        }
        Chromosome newChromo = new Chromosome();
        newChromo.chromosomeGenes.addAll(uniqueGenes);

        return newChromo;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    @Deprecated
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeMath(Object... params) {
        if (params.length != 2) {
            // TODO make a custom Exception for this state
            throw new IllegalStateException("MISSING PARAMS");
        }

        int totalBlocks = (int) params[0];
        int L = (int) params[1]; // messageLength

        final int N = totalBlocks * USEABLE_COEFF_PER_BLOCK;

        final int NL = (int) Math.floor((double) N / L);

        List<AbstractGene<?>> generatedGenes = new ArrayList<>();

        for (int i = 0; i < L; i++) {

            int pos = (i * NL) % N;

            int blockIndex = pos / USEABLE_COEFF_PER_BLOCK;
            int coeffIndex = 1 + (pos % USEABLE_COEFF_PER_BLOCK);

            generatedGenes.add(new Gene(coeffIndex, blockIndex));
        }

        Chromosome newChromo = new Chromosome();
        for (AbstractGene<?> gene : generatedGenes) {
            newChromo.addGene(gene);
        }

        return newChromo;
    }

    /**
     *
     * @param chro1
     * @param chro2
     * @param params
     */
    @Override
    public void crossover(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params) {
        int totalBlocks = (int) params[0];
        List<int[]> validPositions = (List<int[]>) params[1];

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
                Gene emergencyGene = getRandomGene(validPositions, totalBlocks);

                while (this.getGenes().contains(emergencyGene)) {
                    emergencyGene = getRandomGene(validPositions, totalBlocks);
                }
                this.addGene(emergencyGene);
            }
        }
    }

    @Override
    public void tourmentSelection(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2, Object... params) {

    }

    /**
     *
     * @param params
     */
    @Override
    @SuppressWarnings("unchecked")
    public void mutate(Object... params) {
        int totalBlocks = (int) params[0];
        double mutationRate = (double) params[1];
        List<int[]> validPositions = (params.length > 2 && params[2] != null) ? (List<int[]>) params[2] : null;
        boolean lessThan50Attempts = true;
        for (int i = 0; i < this.getNumGenes(); i++) {
            Gene currentGene = (Gene) this.getGeneByIndex(i);

            if (random.nextDouble() >= mutationRate)
                continue;

            Gene candidate;
            int attempts = 0;
            do {
                int newBlock;
                int newCoeff;

                if (validPositions != null && !validPositions.isEmpty()) {
                    int[] pos = validPositions.get(random.nextInt(validPositions.size()));
                    newBlock = pos[0];
                    newCoeff = pos[1];
                } else {
                    newBlock = random.nextInt(totalBlocks);
                    newCoeff = 1 + random.nextInt(USEABLE_COEFF_PER_BLOCK);
                }

                candidate = new Gene(newCoeff, newBlock);
                attempts++;

                if (attempts > 50) {
                    candidate = currentGene;
                    lessThan50Attempts = false;
                }
            } while (this.getGenes().contains(candidate) && lessThan50Attempts);

            currentGene.setBlockIndex(candidate.getBlockIndex());
            currentGene.setValue(candidate.getCoefficientIndex());
        }
    }

    private Gene getRandomGene(List<int[]> validPositions, int totalBlocks) {
        int randomBlock;
        int randomCoeff;
        if (validPositions != null && !validPositions.isEmpty()) {
            // Pick from positions known to have non-zero coefficients
            int[] pos = validPositions.get((int) (Math.random() * validPositions.size()));
            randomBlock = pos[0];
            randomCoeff = pos[1];
        } else {
            // Fallback: random selection (used in tests or when pool isn't available)
            randomBlock = (int) (Math.random() * totalBlocks);
            randomCoeff = 1 + (int) (Math.random() * USEABLE_COEFF_PER_BLOCK);
        }
        return new Gene(randomCoeff, randomBlock);
    }

    @Override
    public String toString() {
        return "Chromosome{genes=" + chromosomeGenes.size() + ", fitness=" + String.format("%.4f", fitnessScore)
                + " Gene= " + this.chromosomeGenes + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
