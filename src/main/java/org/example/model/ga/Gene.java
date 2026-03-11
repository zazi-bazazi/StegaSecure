package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;

public class Gene extends AbstractGene<Integer> {
    private static final int COEFF_MIN = 10;
    private static final int COEFF_MAX = 50;

    private int blockIndex;


    public Gene(Integer value, Object... params) {
        super(value);
        this.blockIndex = (int) params[0];
    }

    public int getBlockIndex() {
        return this.blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public int getCoefficientIndex() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene gene)) return false;
        return blockIndex == gene.blockIndex && this.value == gene.value;
    }

    @Override
    public int hashCode() {
        return 31 * this.blockIndex + this.value;
    }

    @Override
    public String toString() {
        return "(" + this.blockIndex + "," + this.value + ")";
    }

    public static AbstractChromosome<?> buildMidFrequencyPool(int totalBlocks) {
        AbstractChromosome<?> pool = new Chromosome();
        for (int block = 0; block < totalBlocks; block++) {
            for (int coeff = COEFF_MIN; coeff <= COEFF_MAX; coeff++) {
                pool.addGene(new Gene(block, coeff));
            }
        }
        return pool;
    }
}
