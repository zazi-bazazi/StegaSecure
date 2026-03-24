package org.example.test;

import org.example.model.stego.Engine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EngineUtilsTest {

    @Test
    public void testTextToBinaryRoundTrip() {
        String original = "Hello, World!";
        String bits = Engine.textToBinaryString(original);

        // Should have 32-bit header + 13 chars * 8 bits = 136 total bits
        assertEquals(32 + (original.length() * 8), bits.length(),
                "Binary length mismatch: expected 32-bit header + 8 bits per char.");

        // Extract length from header
        int charCount = Integer.parseInt(bits.substring(0, 32), 2);
        assertEquals(original.length(), charCount,
                "Length header doesn't match original text length.");

        // Decode payload manually
        StringBuilder decoded = new StringBuilder();
        String payload = bits.substring(32);
        for (int i = 0; i + 8 <= payload.length(); i += 8) {
            int charCode = Integer.parseInt(payload.substring(i, i + 8), 2);
            decoded.append((char) charCode);
        }
        assertEquals(original, decoded.toString(),
                "Binary encoding/decoding round trip failed!");

        System.out.println("SUCCESS: Text → Binary → Text round trip: \"" + decoded + "\"");
    }

    @Test
    public void testTextToBinaryEmptyString() {
        String bits = Engine.textToBinaryString("");

        // Should have just the 32-bit header with value 0
        assertEquals(32, bits.length());
        assertEquals(0, Integer.parseInt(bits, 2));

        System.out.println("SUCCESS: Empty string produces 32-bit zero header.");
    }

    @Test
    public void testTextToBinarySpecialCharacters() {
        String original = "!@#$%^&*()";
        String bits = Engine.textToBinaryString(original);

        assertEquals(32 + (original.length() * 8), bits.length());

        // Verify header
        int charCount = Integer.parseInt(bits.substring(0, 32), 2);
        assertEquals(original.length(), charCount);

        System.out.println("SUCCESS: Special characters encode correctly.");
    }

    @Test
    public void testTextToBinarySingleChar() {
        String bits = Engine.textToBinaryString("A");

        // 32 header + 8 bits = 40
        assertEquals(40, bits.length());

        // 'A' = 65 = 01000001
        String charBits = bits.substring(32);
        assertEquals("01000001", charBits, "'A' should encode to 01000001");

        System.out.println("SUCCESS: Single character 'A' encodes correctly.");
    }
}
