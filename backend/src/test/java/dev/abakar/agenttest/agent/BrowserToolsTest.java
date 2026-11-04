package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.claude.ClaudeReply.ToolCall;
import dev.abakar.agenttest.report.ReportValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class BrowserToolsTest {

    private final RecordingBrowserSession session = new RecordingBrowserSession();
    private final BrowserTools tools = new BrowserTools(session, new AllowedSites(Set.of("localhost")),
            new ReportValidator(Validation.buildDefaultValidatorFactory().getValidator()));

    private ToolResult call(String name, Map<String, Object> input) {
        return tools.execute(new ToolCall("toolu_1", name, input));
    }

    @Test
    void opensAllowedUrl() {
        ToolResult result = call("ouvrir", Map.of("url", "http://localhost:4200"));

        assertFalse(result.error());
        assertEquals(List.of("open http://localhost:4200"), session.calls);
    }

    @Test
    void refusesUrlOutsideAllowedSites() {
        ToolResult result = call("ouvrir", Map.of("url", "https://example.com"));

        assertTrue(result.error());
        assertTrue(session.calls.isEmpty());
    }

    @Test
    void refusesUnknownTool() {
        ToolResult result = call("supprimer_fichier", Map.of());

        assertTrue(result.error());
        assertTrue(session.calls.isEmpty());
    }

    @Test
    void reportsMissingParameterAsError() {
        assertTrue(call("remplir", Map.of("selecteur", "#q")).error());
    }

    @Test
    void readsFillsAndClicks() {
        call("lire_page", Map.of());
        call("remplir", Map.of("selecteur", "[data-testid=\"search\"]", "texte", "casque"));
        call("cliquer", Map.of("selecteur", "text=Panier"));

        assertEquals(List.of("read", "fill [data-testid=\"search\"] casque", "click text=Panier"), session.calls);
    }

    @Test
    void turnsBrowserFailureIntoShortError() {
        ToolResult result = call("cliquer", Map.of("selecteur", "#introuvable"));

        assertTrue(result.error());
        assertEquals("Timeout 5000ms exceeded.", result.text());
    }

    @Test
    void keepsTheRealCauseOfAPlaywrightError() {
        String playwright = """
                Error {
                  message='net::ERR_CONNECTION_REFUSED at http://localhost:4200/
                Call log:
                  - navigating to "http://localhost:4200/", waiting until "load"'
                  name='Error'
                }""";

        assertEquals("net::ERR_CONNECTION_REFUSED at http://localhost:4200/", BrowserTools.shorten(playwright));
    }

    @Test
    void returnsCaptureAsImageBlock() {
        Map<String, Object> block = call("capture", Map.of()).toBlock("toolu_9");

        assertEquals("toolu_9", block.get("tool_use_id"));
        List<?> content = (List<?>) block.get("content");
        assertEquals("image", ((Map<?, ?>) content.getFirst()).get("type"));
    }

    @Test
    void recordsValidBug() {
        ToolResult result = call("signaler_bug", Map.of("spec", "SPEC-01", "title", "Recherche sensible à la casse",
                "expected", "Le casque apparaît", "actual", "Aucun produit", "severity", "MAJEURE",
                "steps", List.of("Chercher casque")));

        assertFalse(result.error());
        assertEquals(1, tools.bugs().size());
    }

    @Test
    void refusesBugThatFailsValidation() {
        ToolResult result = call("signaler_bug", Map.of("spec", "spec 1", "title", "Titre",
                "expected", "a", "actual", "b", "severity", "MAJEURE", "steps", List.of("x")));

        assertTrue(result.error());
        assertTrue(tools.bugs().isEmpty());
    }

    @Test
    void refusesUnknownSeverity() {
        ToolResult result = call("signaler_bug", Map.of("spec", "SPEC-01", "title", "Titre",
                "expected", "a", "actual", "b", "severity", "GRAVE", "steps", List.of("x")));

        assertTrue(result.error());
    }

    @Test
    void definesExactlyThePlannedTools() {
        List<Object> names = BrowserTools.DEFINITIONS.stream().map(tool -> tool.get("name")).toList();

        assertEquals(List.of("ouvrir", "lire_page", "cliquer", "remplir", "choisir", "capture", "signaler_bug"),
                names);
    }

    @Test
    void choosesOptionInDropDown() {
        ToolResult result = call("choisir", Map.of("selecteur", "[data-testid=\"sort\"]", "option", "asc"));

        assertFalse(result.error());
        assertEquals(List.of("select [data-testid=\"sort\"] asc"), session.calls);
    }
}
