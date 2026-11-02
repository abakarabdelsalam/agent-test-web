package dev.abakar.agenttest.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class AllowedSitesTest {

    private final AllowedSites sites = new AllowedSites(Set.of("localhost"));

    @Test
    void allowsConfiguredHost() {
        assertTrue(sites.allows("http://localhost:4200/panier"));
    }

    @Test
    void refusesOtherHost() {
        assertFalse(sites.allows("https://example.com"));
    }

    @Test
    void refusesHostHiddenBehindCredentials() {
        assertFalse(sites.allows("http://localhost@example.com"));
    }

    @Test
    void refusesFileScheme() {
        assertFalse(sites.allows("file:///C:/Users/secret.txt"));
    }

    @Test
    void parsesFrenchPrice() {
        assertEquals(229.0, CatalogExplorer.parsePrice("229,00 €"));
    }
}
