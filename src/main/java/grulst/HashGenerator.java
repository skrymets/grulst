/*
 * Copyright 2025 The Original Author or Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grulst;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class HashGenerator {
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int GROUPS = 4;
    private static final int GROUP_LENGTH = 4; // Each group is 4 chars
    private static final int NODE_ID_LENGTH = 2; // e.g., for 36^2 = 1296 unique nodes

    private final SecureRandom random;
    private final long nodeId;
    private final AtomicLong sequenceCounter;
    private volatile long lastTimestampMillis;

    private HashGenerator(Builder builder) {
        this.random = new SecureRandom();
        this.nodeId = builder.nodeId;
        this.sequenceCounter = new AtomicLong(0);
        this.lastTimestampMillis = -1;
    }

    /**
     * Returns a new builder with default settings.
     * @return a new HashGenerator.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for HashGenerator.
     */
    public static class Builder {
        private long nodeId;

        private Builder() {
            // Default node ID is random
            this.nodeId = new SecureRandom().nextInt((int) Math.pow(CHARSET.length(), NODE_ID_LENGTH));
        }

        /**
         * Sets the node ID for the HashGenerator.
         * @param nodeId the node ID to use
         * @return this builder instance
         */
        public Builder nodeId(long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /**
         * Builds a new HashGenerator with the configured settings.
         * @return a new HashGenerator instance
         */
        public HashGenerator build() {
            return new HashGenerator(this);
        }
    }

    /**
     * Converts a given long value to a Base36-encoded string representation and pads or truncates
     * it to the desired length.
     *
     * @param value the numerical value to be converted to Base36 encoding
     * @param length the desired length of the resulting Base36-encoded string; the output
     *               will be padded with leading characters or truncated to match this length
     * @return a string representing the Base36-encoded value, padded or truncated to the specified length
     */
    private static String toBase36(long value, int length) {
        StringBuilder sb = new StringBuilder();
        int base = CHARSET.length();
        if (value == 0) {
            sb.append(CHARSET.charAt(0));
        } else {
            while (value > 0) {
                sb.insert(0, CHARSET.charAt((int) (value % base)));
                value /= base;
            }
        }
        // Pad with '0' on the left
        while (sb.length() < length) {
            sb.insert(0, CHARSET.charAt(0));
        }
        // Truncate if too long (shouldn't happen if length is calculated correctly)
        if (sb.length() > length) {
            return sb.substring(sb.length() - length);
        }
        return sb.toString();
    }

    /**
     * Static convenience method that creates a HashGenerator with default settings
     * and generates a hash.
     *
     * @return a generated hash
     */
    public static String generateHash() {
        return builder().build().generate();
    }

    /**
     * Generates a hash using this HashGenerator's configuration.
     *
     * @return a generated hash
     */
    public String generate() {
        StringBuilder sb = new StringBuilder();

        // --- Part 1: Timestamp (for sortability) ---
        long currentMillis = Instant.now().toEpochMilli(); // High-res timestamp

        // Reset counter if timestamp changes
        if (currentMillis != lastTimestampMillis) {
            sequenceCounter.set(0);
            lastTimestampMillis = currentMillis;
        }

        // Get and increment sequence number (for uniqueness within same millisecond on this node)
        long sequence = sequenceCounter.getAndIncrement();
        // Cap sequence to avoid overflowing allocated chars
        long maxSequence = (long) Math.pow(CHARSET.length(), 2) - 1; // 2 chars for sequence (36^2 - 1 = 1295)
        if (sequence > maxSequence) {
            // If too many IDs requested in a single millisecond, wrap around.
            // In a real scenario, consider waiting for the next millisecond or throwing an error.
            sequence = sequence % (maxSequence + 1); // Wrap around, which introduces collision risk
        }

        // Allocate characters for timestamp, node ID, sequence, and random
        // Example allocation:
        // Group 1: Timestamp (first 4 chars)
        // Group 2: Timestamp (next 4 chars)
        // Group 3: Node ID (2 chars) + Sequence (2 chars)
        // Group 4: Random (4 chars)

        // Total 16 characters:
        // Timestamp (milliseconds) needs ~8 chars Base36 for current time range
        String timePart = toBase36(currentMillis, 8); // 8 characters for timestamp

        String nodeIdPart = toBase36(nodeId, NODE_ID_LENGTH); // 2 characters for node ID
        String sequencePart = toBase36(sequence, 2); // 2 characters for sequence

        // Ensure these parts always have the correct length
        timePart = timePart.substring(Math.max(0, timePart.length() - 8)); // Take last 8 chars if too long
        nodeIdPart = nodeIdPart.substring(Math.max(0, nodeIdPart.length() - NODE_ID_LENGTH));
        sequencePart = sequencePart.substring(Math.max(0, sequencePart.length() - 2));

        // Fill remaining with random characters
        StringBuilder randomPart = new StringBuilder();
        int remainingRandomChars = (GROUPS * GROUP_LENGTH) - (timePart.length() + nodeIdPart.length() + sequencePart.length());
        for (int i = 0; i < remainingRandomChars; i++) {
            int index = random.nextInt(CHARSET.length());
            randomPart.append(CHARSET.charAt(index));
        }

        // Assemble the hash in aaaaa-bbbb-cccc-dddd format
        String part1 = timePart.substring(0, 4);
        String part2 = timePart.substring(4, 8);

        // Combine node ID and sequence, then fill with random if needed
        StringBuilder part3_combined = new StringBuilder(nodeIdPart + sequencePart);
        // This logic ensures part3_combined is exactly GROUP_LENGTH (4)
        if (part3_combined.length() < GROUP_LENGTH) {
            for (int i = 0; i < GROUP_LENGTH - part3_combined.length(); i++) {
                part3_combined.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
            }
        } else if (part3_combined.length() > GROUP_LENGTH) {
            part3_combined = new StringBuilder(part3_combined.substring(0, GROUP_LENGTH));
        }

        // The last group is pure random
        // Ensure randomPart has enough chars. It should have 4 for the last group.
        StringBuilder part4 = new StringBuilder(randomPart.substring(0, Math.min(4, randomPart.length())));
        // If for some reason randomPart is less than 4, pad it.
        while (part4.length() < 4) {
            part4.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }


        sb.append(part1).append("-")
                .append(part2).append("-")
                .append(part3_combined).append("-")
                .append(part4);

        return sb.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Generating 5 hash codes using static method (default node ID):");
        for (int i = 0; i < 5; i++) {
            System.out.println(generateHash());
            Thread.sleep(1); // Introduce a 1ms delay to ensure distinct timestamps for sortability
        }

        System.out.println("\nGenerating 5 hash codes using builder with default node ID:");
        HashGenerator generator1 = builder().build();
        for (int i = 0; i < 5; i++) {
            System.out.println(generator1.generate());
            Thread.sleep(1);
        }

        System.out.println("\nGenerating 5 hash codes using builder with custom node ID (42):");
        HashGenerator generator2 = builder().nodeId(42).build();
        for (int i = 0; i < 5; i++) {
            System.out.println(generator2.generate());
            Thread.sleep(1);
        }

        System.out.println("\nGenerating 5 hash codes using builder with another custom node ID (99):");
        HashGenerator generator3 = builder().nodeId(99).build();
        for (int i = 0; i < 5; i++) {
            System.out.println(generator3.generate());
            Thread.sleep(1);
        }
    }
}
