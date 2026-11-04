package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AgentTraceTest {

    private final List<String> console = new ArrayList<>();

    @Test
    void printsNothingWhenObservationIsOff() {
        AgentTrace trace = new AgentTrace(false, console::add);

        trace.start("http://localhost:4200", "Cherche casque");
        trace.action(1, 5, "lire_page", Map.of(), ToolResult.ok("Catalogue"));
        trace.stop("end_turn", 1, 0.001);

        assertTrue(console.isEmpty());
    }

    @Test
    void printsActionNumberToolParametersAndResult() {
        new AgentTrace(true, console::add).action(2, 5, "remplir",
                Map.of("texte", "casque"), ToolResult.ok("Champ rempli."));

        assertEquals("Action 2/5 : remplir {texte=casque} -> Champ rempli.", console.getFirst());
    }

    @Test
    void marksErrorsAndKeepsOneShortLine() {
        new AgentTrace(true, console::add).action(1, 5, "lire_page", Map.of(),
                ToolResult.error("Texte\nsur plusieurs lignes " + "x".repeat(300)));

        String line = console.getFirst();
        assertTrue(line.contains("ERREUR Texte sur plusieurs lignes"));
        assertFalse(line.contains("\n"));
        assertTrue(line.length() <= 161);
    }

    @Test
    void neverPrintsAnApiKey() {
        new AgentTrace(true, console::add).thought("ma clé est sk-ant-api03-ABC_def-123");

        assertEquals("Le modèle dit : ma clé est [clé masquée]", console.getFirst());
    }

    @Test
    void explainsEachStopReasonInFrench() {
        assertEquals("le modèle a terminé", AgentTrace.reason("end_turn"));
        assertEquals("limite d'actions atteinte", AgentTrace.reason("action_limit"));
        assertEquals("réponse coupée par la limite de jetons", AgentTrace.reason("max_tokens"));
        assertEquals("le modèle a refusé la demande", AgentTrace.reason("refusal"));
        assertEquals("limite de durée atteinte", AgentTrace.reason("time_limit"));
        assertEquals("limite de budget atteinte", AgentTrace.reason("budget_limit"));
        assertEquals("erreur de l'API", AgentTrace.reason("api_error"));
    }

    @Test
    void showsEstimatedCostWhenStopping() {
        new AgentTrace(true, console::add).stop("budget_limit", 3, 0.0512);

        assertEquals("Arrêt après 3 action(s) : limite de budget atteinte (coût estimé : 0,0512 $)",
                console.getFirst());
    }
}
