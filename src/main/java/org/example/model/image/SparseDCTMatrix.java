package org.example.model.image;

import java.util.ArrayList;
import java.util.ArrayList;

public class SparseDCTMatrix {
    private final ArrayList<ArrayList<DCTNode>> adjacencyMatrix;

    private final int totalBlocks;
    private final int imageWidth;
    private final int imageHeight;

    public SparseDCTMatrix(int width, int height) {
        this.imageHeight = height;
        this.imageWidth = width;
        this.totalBlocks = calculateTotalBlocks(width, height);

        this.adjacencyMatrix = new ArrayList<>(totalBlocks);

        for (int i = 0; i < totalBlocks; i++) {
            adjacencyMatrix.add(new ArrayList<>());
        }
    }

    public SparseDCTMatrix(SparseDCTMatrix original) {
        this.imageWidth = original.imageWidth;
        this.imageHeight = original.imageHeight;
        this.totalBlocks = calculateTotalBlocks(original.imageWidth, original.imageHeight);

        this.adjacencyMatrix = new ArrayList<>(this.totalBlocks);

        for (int i = 0; i < this.totalBlocks; i++) {
            ArrayList<DCTNode> newBlockList = new ArrayList<>();
            ArrayList<DCTNode> originalBlockList = original.getNonZeroCoefficientsForBlock(i);
            for (DCTNode oldNode : originalBlockList) {
                newBlockList.add(new DCTNode(oldNode.getCoefficientIndex(), oldNode.getValue()));
            }
            this.adjacencyMatrix.add(newBlockList);
        }
    }

    public void setCoefficient(int blockIndex, int coeffIndex, double value) {
        if (value == 0.0) return;
        ArrayList<DCTNode> blockList = adjacencyMatrix.get(blockIndex);
        for (DCTNode node : blockList) {
            if (node.getCoefficientIndex() == coeffIndex) {
                node.setValue(value);
                return;
            }
        }

        blockList.add(new DCTNode(coeffIndex, value));
    }

    public double getCoefficient(int blockIndex, int coeffIndex) {
        ArrayList<DCTNode> blockList = adjacencyMatrix.get(blockIndex);
        for (DCTNode node : blockList) {
            if (node.getCoefficientIndex() == coeffIndex) {
                return node.getValue();
            }
        }
        return 0.0;
    }

    public ArrayList<DCTNode> getNonZeroCoefficientsForBlock(int blockIndex) {
        return adjacencyMatrix.get(blockIndex);
    }

    private static int calculateTotalBlocks(int width, int height) {
        int blocksX = (int) Math.ceil(width / 8.0);
        int blocksY = (int) Math.ceil(height / 8.0);

        return blocksX * blocksY;
    }

    public int getWidth() {
        return this.imageWidth;
    }

    public int getHeight() {
        return this.imageHeight;
    }


}