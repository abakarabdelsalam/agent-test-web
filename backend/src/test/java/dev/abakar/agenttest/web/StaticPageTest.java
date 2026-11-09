package dev.abakar.agenttest.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class StaticPageTest {

    private static String page() throws IOException {
        return new ClassPathResource("static/index.html").getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void servesTheHistoryPage() throws IOException {
        assertTrue(page().contains("Historique de l'agent de test"));
    }

    // Les textes viennent de l'agent et des sites testés : jamais insérés comme du HTML.
    @Test
    void neverRendersAgentTextAsHtml() throws IOException {
        assertFalse(page().contains("innerHTML"));
    }
}
