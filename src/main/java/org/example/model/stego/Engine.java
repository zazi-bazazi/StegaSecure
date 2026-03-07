package org.example.model.stego;

import org.example.model.ga.Population;
import org.example.model.ga.interfaces.IChromosome;
import org.example.model.ga.interfaces.IGene;
import org.example.model.image.ImageProcessor;
import org.example.model.image.MatrixCoordinate;
import org.example.model.image.SparseDCTMatrix;
import org.example.model.image.SpatialMatrix;

import java.util.List;
import java.util.Map;

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

    public double evaluateChromosome(IChromosome chromosome, String secretBits) {
        SparseDCTMatrix tempDCT = new SparseDCTMatrix(this.frequencyDomain);
        List<IGene> genes = chromosome.getGenes();
        for (int i = 0; i < secretBits.length(); i++) {
            IGene targetGene = genes.get(i);
            char secretBit = secretBits.charAt(i);

            double modifiedCoefficient = ParityModifier(tempDCT.getCoefficient(targetGene.getBlockIndex(), targetGene.getCoefficientIndex()), secretBit);
            tempDCT.setCoefficient(targetGene.getBlockIndex(), targetGene.getCoefficientIndex(), modifiedCoefficient);
        }

        SpatialMatrix stegoImage = ImageProcessor.getInstance().convertToSpatialDomain(tempDCT);

        double psnrScore = ImageMetrics.calculatePSNR(this.spatialDomain, stegoImage);
        if (psnrScore < MINIMAL_QUALITY) psnrScore = psnrScore / PENALTY;
        chromosome.setFitnessScore(psnrScore);

        return psnrScore;
    }

    private double ParityModifier(double coefficient, char bit) {
        int intCoef = (int)Math.round(coefficient);
        if ((intCoef & 1) == bit - '0') {
            return (double)intCoef;
        }
        return (double)(intCoef - 1);
    }

}
