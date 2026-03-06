package org.example.model.ga;

import org.example.model.ga.interfaces.IGene;

public class Gene implements IGene {
    private final int blockIndex;
    private final int coefficientIndex;

    public Gene(int blockIndex, int coefficientIndex) {
        this.blockIndex = blockIndex;
        this.coefficientIndex = coefficientIndex;
    }

    @Override
    public int getBlockIndex() {
        return blockIndex;
    }

    @Override
    public int getCoefficientIndex() {
        return coefficientIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene gene)) return false;
        return blockIndex == gene.blockIndex && coefficientIndex == gene.coefficientIndex;
    }

    @Override
    public int hashCode() {
        return 31 * blockIndex + coefficientIndex;
    }

    @Override
    public String toString() {
        return "(" + blockIndex + "," + coefficientIndex + ")";
    }
}
