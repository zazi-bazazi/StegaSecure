package org.example.model.image;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.LinkedList;

public class SparseDCTMatrix {
    // The Adjacency List representation of the matrix
    private final ArrayList<LinkedList<DCTNode>> adjacencyMatrix;
    private final int totalBlocks;

    public SparseDCTMatrix(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.adjacencyMatrix = new ArrayList<>(totalBlocks);

        // Initialize an empty linked list for every block
        for (int i = 0; i < totalBlocks; i++) {
            adjacencyMatrix.add(new LinkedList<>());
        }
    }

    // Add a value to the specific block's linked list
    public void addCoefficient(int blockIndex, int coeffIndex, double value) {
        if (value == 0.0) return; // The core rule of sparse matrices

        LinkedList<DCTNode> blockList = adjacencyMatrix.get(blockIndex);

        // Optional: Check if the coefficient already exists to update it,
        // otherwise add a new node to the end of the list.
        for (DCTNode node : blockList) {
            if (node.getCoefficientIndex() == coeffIndex) {
                node.setValue(value);
                return;
            }
        }

        blockList.add(new DCTNode(coeffIndex, value));
    }

    // Retrieve a value from the adjacency list
    public double getCoefficient(int blockIndex, int coeffIndex) {
        LinkedList<DCTNode> blockList = adjacencyMatrix.get(blockIndex);

        // Traverse the linked list to find the coefficient
        for (DCTNode node : blockList) {
            if (node.getCoefficientIndex() == coeffIndex) {
                return node.getValue();
            }
        }

        // If it's not in the linked list, it means it's a zero!
        return 0.0;
    }

    // This is super useful for the Genetic Algorithm later
    public LinkedList<DCTNode> getNonZeroCoefficientsForBlock(int blockIndex) {
        return adjacencyMatrix.get(blockIndex);
    }
}