package org.example;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.stego.Engine;

import java.io.File;
import java.util.Scanner;

public class Main {
    private static String path;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Engine engine = Engine.getInstance();

        System.out.println("==================================================");
        System.out.println("     DCT STEGANOGRAPHY & GENETIC ALGORITHM        ");
        System.out.println("==================================================");
        System.out.println("  [1] Encode — hide a secret message in an image");
        System.out.println("  [2] Decode — extract a hidden message from an image");
        System.out.println("==================================================");
        System.out.print("\nChoose an option (1 or 2): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> runEncode(scanner, engine);
            case "2" -> runDecode(scanner, engine);
            default  -> System.err.println("[ERROR] Invalid option. Please enter 1 or 2.");
        }
//        runDecode(scanner, engine);

        scanner.close();
    }

    // -------------------------------------------------------------------------
    // ENCODE
    // -------------------------------------------------------------------------
    private static void runEncode(Scanner scanner, Engine engine) {
        System.out.println("\n--- ENCODE MODE ---");

        System.out.print("Enter the secret text to hide: ");
        String secretText = scanner.nextLine();

        System.out.print("Enter the cover image path (e.g., C:/images/cover.png): ");
        String inputImagePath = scanner.nextLine();

        System.out.print("Enter the output directory (e.g., C:/images/output/): ");
        String outputPath = scanner.nextLine();

        try {
            path = engine.encode(secretText, inputImagePath, outputPath);
        } catch (Exception e) {
            System.err.println("\n[FATAL ERROR] Encoding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // DECODE
    // -------------------------------------------------------------------------
    private static void runDecode(Scanner scanner, Engine engine) {
        System.out.println("\n--- DECODE MODE ---");

        System.out.print("Enter the stego image path (.png): ");
        String stegoImagePath = scanner.nextLine();
//        String stegoImagePath = path;

        System.out.print("Enter the key file path (.key): ");
        String keyFilePath = scanner.nextLine();
//        String keyFilePath = path.split("\\.")[0] + ".key";
        try {
            AbstractChromosome<?> chromosome = engine.loadChromosomeKey(new File(keyFilePath));
            String recovered = engine.decode(stegoImagePath, chromosome);

            System.out.println("\n==================================================");
            System.out.println("  RECOVERED MESSAGE: " + recovered);
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("\n[FATAL ERROR] Decoding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}