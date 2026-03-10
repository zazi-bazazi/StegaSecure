package org.example.model.stego;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.example.model.ga.GeneticAlgorithm;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractGene;
import org.example.model.image.ImageProcessor;
import org.example.model.image.SparseDCTMatrix;
import org.example.model.image.SpatialMatrix;

import java.util.ArrayList;

public class Engine {
    private static Engine instance = null;

    private Population population;
    private SparseDCTMatrix frequencyDomain;
    private SpatialMatrix spatialDomain;

    private static final double MINIMAL_QUALITY = 35.0;
    private static final double PENALTY = 2.0;

    private Engine() {
    }

    public static synchronized Engine getInstance() {
        if (instance == null) {
            instance = new Engine();
        }
        return instance;
    }
    public void startEvolution(String secretBits, int totalBlocks) {
        GeneticAlgorithm ga = new GeneticAlgorithm();

        Population emptyPop = new Population();
        Chromosome prototype = new Chromosome();

        int popSize = 100;

        ga.initializePopulation(emptyPop, prototype, popSize, totalBlocks, secretBits.length());

        ga.runGeneration(chromo -> evaluate((Chromosome) chromo, secretBits));
    }

    private double evaluate(Chromosome chromosome, String secretBits) {
        SparseDCTMatrix tempDCT = new SparseDCTMatrix(this.frequencyDomain);
        ArrayList<AbstractGene<?>> genes = chromosome.getGenes();

        int embedded = 0;
        int skipped = 0;

        for (int i = 0; i < secretBits.length(); i++) {
                Gene targetGene = (Gene) genes.get(i);

            double original = tempDCT.getCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex()
            );

            if (Math.abs(original) < 1.0) {
                skipped++;
                continue;
            }

            char secretBit = secretBits.charAt(i);
            double modified = ParityModifier(original, secretBit);
            tempDCT.setCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex(),
                    modified
            );
            embedded++;
        }

        SpatialMatrix stegoImage = ImageProcessor.getInstance().convertToSpatialDomain(tempDCT);
        double psnrScore = ImageMetrics.calculatePSNR(this.spatialDomain, stegoImage);

        if (psnrScore < MINIMAL_QUALITY) psnrScore = psnrScore / PENALTY;
        psnrScore -= skipped * PENALTY;
        chromosome.setFitnessScore(psnrScore);

        return psnrScore;
    }

    private double ParityModifier(double coefficient, char bit) {
        int intCoef = (int)Math.round(coefficient);
        if ((intCoef & 1) == bit - '0') {
            return (double)intCoef;
        }
        if (intCoef >= 0) {
            return (intCoef % 2 == 0) ? (double)(intCoef + 1) : (double)(intCoef - 1);
        } else {
            // For negative: -4 → LSB of abs value, adjust carefully
            return (intCoef % 2 == 0) ? (double)(intCoef - 1) : (double)(intCoef + 1);
        }
    }

}
