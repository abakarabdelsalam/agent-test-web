package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.abakar.agenttest.claude.ClaudeReply.Usage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PricingAndSpecificationsTest {

    @Test
    void computesCostFromTokens() {
        // 1 million de jetons lus à 0,10 $ et 1 million écrits à 0,50 $.
        assertEquals(0.60, TestLoops.HAIKU.cost(new Usage(1_000_000, 1_000_000)), 1e-9);
        assertEquals(0.0, TestLoops.HAIKU.cost(Usage.NONE), 1e-9);
    }

    @Test
    void readsSpecificationsFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("specs.md");
        Files.writeString(file, "- SPEC-01 : la recherche ignore la casse.");

        assertTrue(new Specifications(file).text().contains("SPEC-01"));
    }

    @Test
    void startsWithoutSpecificationsWhenFileIsMissing(@TempDir Path dir) {
        assertEquals("", new Specifications(dir.resolve("absent.md")).text());
    }

    @Test
    void projectSpecificationsAreReadableFromBackendFolder() {
        // Les tests Maven tournent dans backend/, comme mvn spring-boot:run.
        assertTrue(new Specifications(Path.of("../specs/specifications.md")).text().contains("SPEC-20"));
    }
}
