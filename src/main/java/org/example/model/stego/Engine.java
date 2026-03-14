package org.example.model.stego;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.example.model.ga.GeneticAlgorithm;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.FitnessFunction;
import org.example.model.image.DCTMath;
import org.example.model.image.ImageProcessor;
import org.example.model.image.SparseDCTMatrix;
import org.example.model.image.SpatialMatrix;

import java.io.*;
import java.util.function.Predicate;

public class Engine {
    private static Engine instance = null;

    private GeneticAlgorithm ga;
    private SparseDCTMatrix frequencyDomain;
    private SpatialMatrix spatialDomain;

    private static final double MINIMAL_QUALITY = 35.0;
    private static final double PENALTY = 2.0;

    private record EmbeddedResult(SparseDCTMatrix matrix, int skipped) {
    }

    private Engine() {
    }

    private void setGeneticAlgorithm(GeneticAlgorithm ga) {
        this.ga = ga;
    }

    public static synchronized Engine getInstance() {
        if (instance == null) {
            instance = new Engine();
        }
        return instance;
    }

    public AbstractChromosome<?> geneticAlgorithmManager(String secretBits, int totalBlocks) {
        Population emptyPop = new Population();
        AbstractChromosome<?> emptyChro = new Chromosome();

        FitnessFunction evaluator = (chromosome) -> this.evaluateFitness((Chromosome) chromosome, secretBits);

        Predicate<AbstractChromosome<?>> stopEarly = (chromosome) -> chromosome.getFitnessScore() >= 60.0;

        GeneticAlgorithm ga = new GeneticAlgorithm(evaluator, totalBlocks, secretBits.length());
        setGeneticAlgorithm(ga);

        AbstractChromosome<?> finalChro = (Chromosome) ga.evolve(emptyPop, emptyChro, stopEarly);

        System.out.println("Finished! Best PSNR: " + finalChro.getFitnessScore());
        System.out.println("Chromosome: " + finalChro);

        return finalChro;
    }

    private EmbeddedResult implementChromosomeToImage(String secretBits, AbstractChromosome<?> chromosome) {
        SparseDCTMatrix tempDCT = new SparseDCTMatrix(this.frequencyDomain);

        int skipped = 0;

        for (int i = 0, bitIndex = 0; i < secretBits.length(); i++) {
            if (i >= chromosome.getNumGenes()) {
                skipped = secretBits.length() - bitIndex;
                break;
            }

            Gene targetGene = (Gene) chromosome.getGeneByIndex(i);

            double original = tempDCT.getCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex());

            if (Math.abs(original) < 1.0) {
                skipped++;
                continue;
            }

            char secretBit = secretBits.charAt(bitIndex);
            double modified = parityModifier(original, secretBit);
            tempDCT.setCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex(),
                    modified);

            bitIndex++;
        }

        return new EmbeddedResult(tempDCT, skipped);
    }

    /**
     * Quantized-Parseval fitness: computes PSNR without any IDCT.
     *
     * Parseval's theorem: spatial-domain SSE = frequency-domain SSE.
     * Since we modify quantized coefficients by Δq (0 or ±1), the real
     * DCT-domain change is Δq × Q[u][v], so:
     *   SSE = Σ (Δq × Q[u][v])²
     *   MSE = SSE / totalPixels
     *   PSNR = 10 × log10(255² / MSE)
     */
    private double evaluateFitness(AbstractChromosome<?> chromosome, String secretBits) {
        double sse = 0.0;
        int skipped = 0;

        for (int i = 0, bitIndex = 0; bitIndex < secretBits.length() && i < chromosome.getNumGenes(); i++) {
            Gene gene = (Gene) chromosome.getGeneByIndex(i);
            double original = this.frequencyDomain.getCoefficient(
                    gene.getBlockIndex(), gene.getCoefficientIndex());

            if (Math.abs(original) < 1.0) {
                skipped++;
                continue;
            }

            double modified = parityModifier(original, secretBits.charAt(bitIndex));
            double deltaQ = modified - Math.round(original); // 0 or ±1 in quantized domain

            // Map zig-zag index → (u,v) → quantization step Q[u][v]
            int qStep = getQuantizationStep(gene.getCoefficientIndex());
            sse += (deltaQ * qStep) * (deltaQ * qStep);

            bitIndex++;
        }

        int totalPixels = this.frequencyDomain.getWidth() * this.frequencyDomain.getHeight();
        double mse = sse / totalPixels;

        double psnrScore;
        if (mse == 0.0 && skipped == 0) {
            psnrScore = Double.MAX_VALUE;
        } else if (mse == 0.0) {
            psnrScore = Math.max(0, MINIMAL_QUALITY - (skipped * PENALTY));
        } else {
            psnrScore = 10.0 * Math.log10(255.0 * 255.0 / mse);
            if (psnrScore < MINIMAL_QUALITY || skipped > 0)
                psnrScore /= PENALTY;
        }

        chromosome.setFitnessScore(psnrScore);
        return psnrScore;
    }

    /**
     * Maps a zig-zag coefficient index (0-63) to its JPEG quantization step value.
     */
    private static int getQuantizationStep(int zigzagIndex) {
        int u = ImageProcessor.ZIGZAG_U[zigzagIndex];
        int v = ImageProcessor.ZIGZAG_V[zigzagIndex];
        return DCTMath.LUMA_QUANTIZATION[u][v];
    }


        private double parityModifier(double coefficient, char bit) {
        int intCoef = (int) Math.round(coefficient);
        if ((intCoef & 1) == bit - '0') {
            return (double) intCoef;
        }
        if (intCoef >= 0) {
            return (intCoef % 2 == 0) ? (double) (intCoef + 1) : (double) (intCoef - 1);
        } else {
            // For negative: -4 → LSB of abs value, adjust carefully
            return (intCoef % 2 == 0) ? (double) (intCoef - 1) : (double) (intCoef + 1);
        }
    }

    public void loadCoverImage(SpatialMatrix spatial, SparseDCTMatrix frequency) {
        this.spatialDomain = spatial;
        this.frequencyDomain = frequency;
    }

    public static String textToBinaryString(String text) {
        StringBuilder binary = new StringBuilder();

        // Prepend 32-bit length header (number of characters, not bits)
        String lengthHeader = String.format("%32s", Integer.toBinaryString(text.length())).replace(' ', '0');
        binary.append(lengthHeader);

        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    private static String binaryStringToText(String bits) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i + 8 <= bits.length(); i += 8) {
            int charCode = Integer.parseInt(bits.substring(i, i + 8), 2);
            text.append((char) charCode);
        }
        return text.toString();
    }

    public String decode(String stegoImagePath, AbstractChromosome<?> chromosome) {
        try {
            // 1. Load the stego image and convert to the frequency domain
            SpatialMatrix stegoImage = new SpatialMatrix(stegoImagePath);
            SparseDCTMatrix stegoFrequency = ImageProcessor.getInstance().convertToFrequencyDomain(stegoImage);

            StringBuilder extractedBits = new StringBuilder();

            // 2. Read bits from every gene position the chromosome points to
            // (same order used during encoding)
            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                Gene gene = (Gene) chromosome.getGeneByIndex(i);
                double coeff = stegoFrequency.getCoefficient(
                        gene.getBlockIndex(),
                        gene.getCoefficientIndex());

                // Skip weak coefficients — they were skipped during encoding too
                if (Math.abs(coeff) < 1.0)
                    continue;

                // Extract the LSB of the rounded coefficient
                int intCoef = (int) Math.round(coeff);
                int lsb = Math.abs(intCoef) & 1;
                extractedBits.append(lsb);
            }

            String allBits = extractedBits.toString();

            // 3. Read the 32-bit length header to know how many characters to recover
            if (allBits.length() < 32) {
                return "[DECODE ERROR] Not enough bits extracted to read the length header.";
            }
            int charCount = Integer.parseInt(allBits.substring(0, 32), 2);
            int bitsNeeded = 32 + (charCount * 8);

            if (allBits.length() < bitsNeeded) {
                return "[DECODE ERROR] Stego image does not contain enough embedded bits. " +
                        "Expected " + bitsNeeded + " but got " + allBits.length() + ".";
            }

            // 4. Skip the header, decode the payload
            String payloadBits = allBits.substring(32, bitsNeeded);
            String recoveredText = binaryStringToText(payloadBits);

            System.out.println("[SUCCESS] Decoded " + charCount + " character(s): " + recoveredText);
            return recoveredText;

        } catch (Exception e) {
            System.err.println("[DECODE ERROR] Failed to decode the stego image.");
            e.printStackTrace();
            return "[DECODE ERROR] " + e.getMessage();
        }
    }

    public void encode(String secretText, String inputImagePath, String outputPath) throws Exception {
        String secretBits = textToBinaryString(secretText);
        System.out.println("\n[INFO] Bits to hide: " + secretBits.length() + " bits.");

        System.out.println("[INFO] Loading image and converting to Frequency Domain...");
        SpatialMatrix spatialImage = new SpatialMatrix(inputImagePath);
        SparseDCTMatrix frequencyImage = ImageProcessor.getInstance().convertToFrequencyDomain(spatialImage);

        int totalBlocks = (int) Math.ceil(spatialImage.getWidth() / 8.0)
                * (int) Math.ceil(spatialImage.getHeight() / 8.0);
        System.out.println("[INFO] Image loaded! Total 8x8 blocks: " + totalBlocks);

        this.loadCoverImage(spatialImage, frequencyImage);

        System.out.println("\n[INFO] Starting Genetic Algorithm Evolution...");
        AbstractChromosome<?> winningChromosome = geneticAlgorithmManager(secretBits, totalBlocks);

        buildAndSaveStegoImage(winningChromosome, secretBits, outputPath);
    }

    public void buildAndSaveStegoImage(AbstractChromosome<?> bestChromosome, String secretBits, String outputPath) {
        System.out.println("\n[INFO] Generating final Master Matrix...");

        EmbeddedResult result = this.implementChromosomeToImage(secretBits, bestChromosome);

        SparseDCTMatrix finalDCT = result.matrix();
        int skipped = result.skipped();

        if (skipped > 0) {
            System.out.println(
                    "[WARNING] The best chromosome still hit " + skipped + " zeroes. Message might be corrupted.");
        } else {
            System.out.println("[SUCCESS] All " + secretBits.length() + " bits locked perfectly into the frequencies!");
        }

        System.out.println("[INFO] Running Inverse DCT to rebuild spatial pixels...");
        SpatialMatrix stegoImage = ImageProcessor.getInstance().convertToSpatialDomain(finalDCT);

        try {
            // Save PNG — lossless, preserves the embedded message
            File imageFile = stegoImage.saveImage(outputPath, "png");

            // Save JPEG preview — uses Java's built-in encoder (no external libraries)
            // Note: This JPEG is for visual preview only; the PNG is the actual carrier.
            File jpegFile = stegoImage.saveImage(outputPath, "jpg");

            // Derive the key file name from the PNG file name (same stem, .key extension)
            String keyFileName = imageFile.getName().replace(".png", ".key");
            File keyFile = new File(imageFile.getParent(), keyFileName);
            saveChromosomeKey(bestChromosome, keyFile);

            System.out.println("\n==================================================");
            System.out.println("  STEGO-IMAGE (PNG) : " + imageFile.getAbsolutePath());
            System.out.println("  JPEG PREVIEW      : " + jpegFile.getAbsolutePath());
            System.out.println("  KEY FILE          : " + keyFile.getAbsolutePath());
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Failed to save outputs to disk.");
            e.printStackTrace();
        }
    }

    public void saveChromosomeKey(AbstractChromosome<?> chromosome, File keyFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            writer.write("# StegaSecure Key File — do not edit");
            writer.newLine();
            writer.write("# Format: blockIndex,coefficientIndex (one gene per line)");
            writer.newLine();

            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                Gene gene = (Gene) chromosome.getGeneByIndex(i);
                writer.write(gene.getBlockIndex() + "," + gene.getCoefficientIndex());
                writer.newLine();
            }
        }
        System.out.println(">>> Key file saved at: " + keyFile.getAbsolutePath());
    }

    public AbstractChromosome<?> loadChromosomeKey(File keyFile) throws IOException {
        Chromosome chromosome = new Chromosome();

        try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith("#") || line.isBlank())
                    continue;

                String[] parts = line.split(",");
                if (parts.length != 2) {
                    throw new IOException("Malformed key file line: \"" + line + "\"");
                }

                int blockIndex = Integer.parseInt(parts[0].trim());
                int coeffIndex = Integer.parseInt(parts[1].trim());
                chromosome.addGene(new Gene(coeffIndex, blockIndex));
            }
        }

        System.out.println(">>> Loaded chromosome key with " + chromosome.getNumGenes() + " genes from: "
                + keyFile.getAbsolutePath());
        return chromosome;
    }

}
