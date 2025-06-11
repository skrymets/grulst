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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the HashGenerator class.
 * <p>
 * The generateHash method generates a unique hash code based on the current timestamp,
 * a node identifier, a sequence counter, and random characters.
 */
public class HashGeneratorTest {
    HashGenerator hashGenerator;

    @BeforeEach
    public void setup() {
        hashGenerator = HashGenerator.builder().nodeId(0).build();
    }

    @Test
    void testHashFormat() {
        String hash = hashGenerator.generate();
        assertTrue(hash.matches("^([a-z0-9]{4}-){3}[a-z0-9]{4}$"),
                "Generated hash should match the format 'xxxx-xxxx-xxxx-xxxx' where x is an alphanumeric character");
    }

    @Test
    void testHashLength() {
        String hash = hashGenerator.generate();
        assertEquals(19, hash.length(),
                "Generated hash should always be 19 characters long, including hyphens");
    }

    @Test
    void testHashesAreUnique() {
        String hash1 = hashGenerator.generate();
        String hash2 = hashGenerator.generate();
        assertNotEquals(hash1, hash2, "Two consecutive hashes should not be identical");
    }

    @Test
    void testNodeIdPartConsistency() {
        String hash1 = hashGenerator.generate();
        String hash2 = hashGenerator.generate();
        String nodePart1 = hash1.split("-")[2].substring(0, 2);
        String nodePart2 = hash2.split("-")[2].substring(0, 2);
        assertEquals(nodePart1, nodePart2, "The node ID portion of the hash should remain consistent for all generated hashes");
    }

    @Test
    void testSequenceIncrements() {
        String hash1 = hashGenerator.generate();
        String hash2 = hashGenerator.generate();
        int sequencePart1 = Integer.parseInt(hash1.split("-")[2].substring(2, 4), 36);
        int sequencePart2 = Integer.parseInt(hash2.split("-")[2].substring(2, 4), 36);
        assertTrue(sequencePart2 == sequencePart1 + 1 || sequencePart2 == 0,
                "The sequence portion should increment by 1, and reset properly when maximum is reached");
    }

    @Test
    void testRandomPartGeneratesCorrectly() {
        String hash = hashGenerator.generate();
        String randomPart = hash.split("-")[3];
        assertTrue(randomPart.matches("^[a-z0-9]{4}$"),
                "The random part of the hash should consist of exactly 4 alphanumeric characters");
    }
}
