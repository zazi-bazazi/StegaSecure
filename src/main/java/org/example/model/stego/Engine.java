package org.example.model.stego;

import org.example.model.ga.Chromosome;
import org.example.model.ga.Gene;
import org.example.model.ga.GeneticAlgorithm;
import org.example.model.ga.Population;
import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.FitnessFunction;
import org.example.model.image.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Engine {
    private static Engine instance = null;

    private GeneticAlgorithm ga;
    private FrequencyDomain frequencyDomain;
    private SpatialDomain spatialDomain;

    private record EmbeddedResult(FrequencyDomain matrix, int skipped) {
    }

    public record FilesRecord(BufferedImage stegoImage, String keyFileInString) {

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

    public AbstractChromosome<?> geneticAlgorithmManager(String secretBits, int totalBlocks) throws Exception {
        Population emptyPop = new Population();
        AbstractChromosome<?> emptyChro = new Chromosome();

        FitnessFunction evaluator = (chromosome) -> this.evaluateFitness((Chromosome) chromosome, secretBits);

        Predicate<AbstractChromosome<?>> stopEarly = (chromosome) -> chromosome.getFitnessScore() >= 60.0;

        // Build pool of non-zero coefficient positions from the frequency domain
        // by traversing the blocks as a graph (BFS) using the spatial adjacency list
        List<int[]> validPositions = new ArrayList<>();
        boolean[] visitedBlocks = new boolean[totalBlocks];
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();

        // Start traversal from the top-left block (Node 0)
        queue.add(0);
        visitedBlocks[0] = true;

        while (!queue.isEmpty()) {
            int block = queue.poll();

            for (DCTNode node : this.frequencyDomain.getNonZeroCoefficientsForBlock(block)) {
                int coeff = node.getCoefficientIndex();

                // Skip the DC coefficient (index 0)
                if (coeff > 0 && Math.abs(node.getValue()) >= 1.0) {
                    validPositions.add(new int[] { block, coeff });
                }
            }

            // Traverse to adjacent blocks using the SparseDCTMatrix adjacency list!
            for (int neighbor : this.frequencyDomain.getNeighborBlocks(block)) {
                if (!visitedBlocks[neighbor]) {
                    visitedBlocks[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }

        System.out.println("[INFO] Valid non-zero coefficient positions: " + validPositions.size()
                + " (need " + secretBits.length() + " bits)");

        if (validPositions.size() < secretBits.length()) {
            throw new Exception("not enough valid positions in image");
        }

        GeneticAlgorithm ga = new GeneticAlgorithm(evaluator, totalBlocks, secretBits.length(), validPositions);
        setGeneticAlgorithm(ga);

        AbstractChromosome<?> finalChro = (Chromosome) ga.evolve(emptyPop, emptyChro, stopEarly);

        System.out.println("Finished! Best PSNR: " + finalChro.getFitnessScore());
        System.out.println("Chromosome: " + finalChro);

        return finalChro;
    }

    /**
     * Embeds the message into the image following the chromosome locations
     * <p>
     * This method iterates through the genes of the provided chromosome, using each
     * gene's
     * indices to locate specific DCT coefficients and modify them via parity
     * encoding.
     * <p>
     * 
     * @param secretBits the message in bits
     * @param chromosome the chromosome used to embed the message in the image
     * @return {@link EmbeddedResult} containing the modified Sparse DCT matrix and
     *         the count of bits that could not be embedded due to chromosome
     *         length.
     * @see #parityModifier(double, char)
     */
    private EmbeddedResult implementChromosomeToImage(String secretBits, AbstractChromosome<?> chromosome) {
        FrequencyDomain tempDCT = new FrequencyDomain(this.frequencyDomain);

        int bitIndex = 0;

        for (int i = 0; i < chromosome.getNumGenes() && bitIndex < secretBits.length(); i++) {
            Gene targetGene = (Gene) chromosome.getGeneByIndex(i);

            double original = tempDCT.getCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex());

            char secretBit = secretBits.charAt(bitIndex);
            double modified = parityModifier(original, secretBit);
            tempDCT.setCoefficient(
                    targetGene.getBlockIndex(),
                    targetGene.getCoefficientIndex(),
                    modified);

            bitIndex++;
        }

        int skipped = secretBits.length() - bitIndex;
        return new EmbeddedResult(tempDCT, skipped);
    }

    /**
     * Quantized-Parseval fitness: computes PSNR without any IDCT.
     * <p>
     * Parseval's theorem: spatial-domain SSE = frequency-domain SSE.
     * Since we modify quantized coefficients by Δq (0 or ±1), the real
     * DCT-domain change is Δq × Q[u][v], so:
     * SSE = Σ (Δq × Q[u][v])²
     * MSE = SSE / totalPixels
     * PSNR = 10 × log10(255² / MSE)
     * <p>
     * quantization table {@link DCTMath#LUMA_QUANTIZATION}
     * </p>
     * 
     * @param chromosome chromosome to evaluate
     * @param secretBits the message in bits
     * @return calculated PSNR score, the higher the score the less visual
     *         distortion.
     * @see ImageMetrics#calculatePSNR(FrequencyDomain, String, int, double)
     */
    private double evaluateFitness(AbstractChromosome<?> chromosome, String secretBits) {
        double sse = 0.0;

        int bitIndex = 0;
        for (int i = 0; i < chromosome.getNumGenes() && bitIndex < secretBits.length(); i++) {
            Gene gene = (Gene) chromosome.getGeneByIndex(i);
            double original = this.frequencyDomain.getCoefficient(
                    gene.getBlockIndex(), gene.getCoefficientIndex());

            double modified = parityModifier(original, secretBits.charAt(bitIndex));
            double deltaQ = modified - Math.round(original); // 0 or ±1 in quantized domain

            // Map zig-zag index → (u,v) → quantization step Q[u][v]
            // int qStep = getQuantizationStep(gene.getCoefficientIndex());
            int qStep = DCTMath.LUMA_QUANTIZATION[ImageProcessor.ZIGZAG_U[gene
                    .getCoefficientIndex()]][ImageProcessor.ZIGZAG_V[gene.getCoefficientIndex()]];
            sse += (deltaQ * qStep) * (deltaQ * qStep);

            bitIndex++;
        }

        double psnrScore = ImageMetrics.calculatePSNR(this.frequencyDomain, secretBits, bitIndex, sse);

        chromosome.setFitnessScore(psnrScore);
        return psnrScore;
    }

    /**
     * Modifies a DCT coefficient to match the parity of the secret bit.
     * <p>
     * If the coefficient's LSB already matches the bit, it remains unchanged.
     * Otherwise, it is incremented or decremented by 1 to flip the parity
     * while minimizing visual distortion (SSE).
     *
     * @param coefficient The original quantized DCT coefficient.
     * @param bit         The secret bit to embed ('0' or '1').
     * @return The modified coefficient as a double.
     */
    private double parityModifier(double coefficient, char bit) {
        int intCoef = (int) Math.round(coefficient);
        if ((Math.abs(intCoef) % 2) == bit - '0') {
            return intCoef;
        }
        if (intCoef > 0) {
            return (double) intCoef - 1;
        } else if (intCoef < 0) {
            return (double) intCoef + 1;
        } else {
            return 1.0;
        }
    }

    public void loadCoverImage(SpatialDomain spatial, FrequencyDomain frequency) {
        this.spatialDomain = spatial;
        this.frequencyDomain = frequency;
    }

    /**
     * parse a text string to a binary string.
     * <p>
     * </p>
     * The first 32 bits represent the total number of characters in the message
     * Each subsequent character is encoded as a fixed-width 8-bit binary string.
     *
     * @param text The message to be converted.
     * @return A bitstream String containing the length header followed by the
     *         payload.
     */
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

    /**
     * Reconstructs a plaintext string from a binary bitstream.
     * <p>
     * </p>
     * This method interprets the bitstream in 8-bit chunks, converting each
     * back into its corresponding character representation.
     * 
     * @param bits bits The raw binary string (excluding the 32-bit header).
     * @return The reconstructed plaintext message.
     */
    private static String binaryStringToText(String bits) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i + 8 <= bits.length(); i += 8) {
            int charCode = Integer.parseInt(bits.substring(i, i + 8), 2);
            text.append((char) charCode);
        }
        return text.toString();
    }

    /**
     * Decodes a hidden message from a stego-image using a specific genetic path.
     * <p>
     * This method extracts the Least Significant Bits (LSB) from the DCT
     * coefficients
     * specified by the chromosome's genes. It expects a 32-bit header indicating
     * the payload length followed by the bitstream of the message.
     *
     * @param stegoImagePath The file path to the image containing the hidden data.
     * @param chromosome     The chromosome acting as the "key" to locate the hidden
     *                       bits.
     * @return The recovered plaintext string, or an error message if
     *         the header is malformed or the bitstream is incomplete.
     * @throws IllegalArgumentException if the image path is invalid or the
     *                                  chromosome is null.
     */
    public String decode(File stegoImagePath, AbstractChromosome<?> chromosome) {
        try {
            SpatialDomain stegoImage = new SpatialDomain(stegoImagePath);
            FrequencyDomain stegoFrequency = ImageProcessor.getInstance().convertToFrequencyDomain(stegoImage);

            StringBuilder extractedBits = new StringBuilder();

            // 2. Read bits from every gene position the chromosome points to
            // (same order used during encoding — no skipping)
            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                Gene gene = (Gene) chromosome.getGeneByIndex(i);
                double coeff = stegoFrequency.getCoefficient(
                        gene.getBlockIndex(),
                        gene.getCoefficientIndex());

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

    /**
     * Encodes the plain Text into the cover image using Steganoraphy.
     * 
     * @param secretText     The plain Text message to be hidden
     * @param inputImagePath The cover image.
     * @return A {@link BufferedImage} containing the hidden data
     * @throws IOException If the image file cannot be read or processed.
     * @see #geneticAlgorithmManager(String, int)
     * @see #implementChromosomeToImage(String, AbstractChromosome)
     * @see ImageProcessor#convertToFrequencyDomain(SpatialDomain)
     */
    public FilesRecord encode(String secretText, File inputImagePath) throws Exception {
        String secretBits = textToBinaryString(secretText);
        System.out.println("\n[INFO] Bits to hide: " + secretBits.length() + " bits.");

        System.out.println("[INFO] Loading image and converting to Frequency Domain...");
        SpatialDomain spatialImage = new SpatialDomain(inputImagePath);
        FrequencyDomain frequencyImage = ImageProcessor.getInstance().convertToFrequencyDomain(spatialImage);

        int totalBlocks = (int) Math.ceil(spatialImage.getWidth() / 8.0)
                * (int) Math.ceil(spatialImage.getHeight() / 8.0);
        System.out.println("[INFO] Image loaded! Total 8x8 blocks: " + totalBlocks);

        this.loadCoverImage(spatialImage, frequencyImage);

        System.out.println("\n[INFO] Starting Genetic Algorithm Evolution...");
        AbstractChromosome<?> winningChromosome = geneticAlgorithmManager(secretBits, totalBlocks);

        // return buildAndSaveStegoImage(winningChromosome, secretBits, outputPath);

        EmbeddedResult result = this.implementChromosomeToImage(secretBits, winningChromosome);

        FrequencyDomain finalDCT = result.matrix();
        int skipped = result.skipped();

        if (skipped > 0) {
            System.out.println(
                    "[WARNING] The best chromosome still hit " + skipped + " zeroes. Message might be corrupted.");
        } else {
            System.out.println("[SUCCESS] All " + secretBits.length() + " bits locked perfectly into the frequencies!");
        }

        System.out.println("[INFO] Running Inverse DCT to rebuild spatial pixels...");
        SpatialDomain stegoImage = ImageProcessor.getInstance().convertToSpatialDomain(finalDCT);

        // Copy the original Cb/Cr channels so the stego image preserves color
        stegoImage.copyChromaFrom(this.spatialDomain);

        return new FilesRecord(stegoImage.saveImage(), generateChromosomeData(winningChromosome));

    }

    @Deprecated
    public String buildAndSaveStegoImage(AbstractChromosome<?> bestChromosome, String secretBits) {
        System.out.println("\n[INFO] Generating final Master Matrix...");

        EmbeddedResult result = this.implementChromosomeToImage(secretBits, bestChromosome);

        FrequencyDomain finalDCT = result.matrix();
        int skipped = result.skipped();

        if (skipped > 0) {
            System.out.println(
                    "[WARNING] The best chromosome still hit " + skipped + " zeroes. Message might be corrupted.");
        } else {
            System.out.println("[SUCCESS] All " + secretBits.length() + " bits locked perfectly into the frequencies!");
        }

        System.out.println("[INFO] Running Inverse DCT to rebuild spatial pixels...");
        SpatialDomain stegoImage = ImageProcessor.getInstance().convertToSpatialDomain(finalDCT);

        // Copy the original Cb/Cr channels so the stego image preserves color
        stegoImage.copyChromaFrom(this.spatialDomain);

        File imageFile = null;
        try {
            // Save PNG — lossless, preserves the embedded message
            String outputPath = "";
            imageFile = stegoImage.saveImage(outputPath, "png");

            // Save JPEG preview — uses Java's built-in encoder (no external libraries)
            // Note: This JPEG is for visual preview only; the PNG is the actual carrier.
            File jpegFile = stegoImage.saveImage(outputPath, "jpg");

            // Derive the key file name from the PNG file name (same stem, .key extension)
            String keyFileName = imageFile.getName().replace(".png", ".key");
            File keyFile = new File(imageFile.getParent(), keyFileName);
            saveChromosomeKey(bestChromosome);

            System.out.println("\n==================================================");
            System.out.println("  STEGO-IMAGE (PNG) : " + imageFile.getAbsolutePath());
            System.out.println("  JPEG PREVIEW      : " + jpegFile.getAbsolutePath());
            System.out.println("  KEY FILE          : " + keyFile.getAbsolutePath());
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Failed to save outputs to disk.");
            e.printStackTrace();
        }
        return imageFile.getAbsolutePath();
    }

    /**
     * Save the Chromosome to a file.
     * <p>
     * </p>
     * Each gene will show in a new line in this format
     * {@code BlockIndex, CoefficientIndex}
     *
     * @param chromosome The optimized chromosome selected by the Genetic Algorithm.
     * @throws IOException IOException If the file cannot be created or written to.
     */
    public void saveChromosomeKey(AbstractChromosome<?> chromosome) throws IOException {
        File file = new File("stegoImage" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // writer.write("# StegaSecure Key File — do not edit");
            // writer.newLine();
            // writer.write("# Format: blockIndex,coefficientIndex (one gene per line)");
            // writer.newLine();

            for (int i = 0; i < chromosome.getNumGenes(); i++) {
                Gene gene = (Gene) chromosome.getGeneByIndex(i);
                writer.write(gene.getBlockIndex() + "," + gene.getCoefficientIndex());
                writer.newLine();
            }
        }
//        System.out.println(">>> Key file saved at: " + keyFile.getAbsolutePath());
        System.out.println(">>> Key file saved ");
    }

    public String generateChromosomeData(AbstractChromosome<?> chromosome) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chromosome.getNumGenes(); i++) {
            Gene gene = (Gene) chromosome.getGeneByIndex(i);
            sb.append(gene.getBlockIndex()).append(",").append(gene.getCoefficientIndex()).append("\n");
        }
        return sb.toString();
    }

    public static void saveToFile(String data, String folderPath, String fileName) throws IOException {
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(data);
        }
        System.out.println("File successfully saved to: " + file.getAbsolutePath());
    }


    /**
     * Reconstructs a chromosome from a serialized key file.
     * <p>
     * </p>
     * File is expected to be in a format that each line represents a single gene in
     * this format {@code blockIndex, coefficientIndex}
     * 
     * @param keyFile file with the chromosome data
     * @return {@link AbstractChromosome} The chromosome containing the data from
     *         the file.
     * @throws IOException           If the file cannot be read or contains
     *                               malformed lines.
     * @throws NumberFormatException If a coordinate in the key file is not a valid
     *                               integer.
     */
    public AbstractChromosome<?> loadChromosomeKey(File keyFile) throws IOException {
        AbstractChromosome<?> chromosome = new Chromosome();

        try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines
                // if (line.startsWith("#") || line.isBlank())
                // continue;

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
