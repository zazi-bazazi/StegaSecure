package org.example.model.image;

public class MatrixCoordinate {
    private final int blockIndex;
    private final int coefficientIndex;

    public MatrixCoordinate(int blockIndex, int coefficientIndex) {
        this.blockIndex = blockIndex;
        this.coefficientIndex = coefficientIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixCoordinate that = (MatrixCoordinate) o;
        return blockIndex == that.blockIndex && coefficientIndex == that.coefficientIndex;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(blockIndex, coefficientIndex);
    }
}