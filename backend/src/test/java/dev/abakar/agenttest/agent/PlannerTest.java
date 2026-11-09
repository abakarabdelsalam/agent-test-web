package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import org.junit.jupiter.api.Test;

class PlannerTest {

    private static Planner plannerAnswering(String text) {
        return new Planner(new ClaudeClient() {
            @Override
            public ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
                return ClaudeReply.endTurn(text);
            }

            @Override
            public String ask(String prompt) {
                return "";
            }

            @Override
            public String mode() {
                return "fake";
            }
        });
    }

    @Test
    void keepsTheStepsAndIgnoresOtherLines() {
        Planner planner = plannerAnswering("Voici le plan :\n- Ouvrir le catalogue\n- Vérifier le panier\nBonne chance.");

        assertEquals(List.of("Ouvrir le catalogue", "Vérifier le panier"), planner.plan("Teste le site").steps());
    }

    @Test
    void keepsAtMostFiveSteps() {
        String text = "- a\n- b\n- c\n- d\n- e\n- f\n- g";

        assertEquals(5, plannerAnswering(text).plan("Teste").steps().size());
    }
}
