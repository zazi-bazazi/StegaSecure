package org.example.model.image;

public class DCTNode {
    private final int coefficientIndex;
    private double value;

    public DCTNode(int coefficientIndex, double value) {
        this.coefficientIndex = coefficientIndex;
        this.value = value;
    }

    public int getCoefficientIndex() {
        return coefficientIndex;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}