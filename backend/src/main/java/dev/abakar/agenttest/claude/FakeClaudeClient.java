package dev.abakar.agenttest.claude;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Réponses simulées, pour développer sans clé API et sans frais. */
@Component
@ConditionalOnProperty(name = "agent.claude.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClaudeClient implements ClaudeClient {

    /** Parcours de démonstration : chercher « casque » dans TechShop. */
    static final List<ClaudeReply> DEMO_SCENARIO = List.of(
            ClaudeReply.toolUse("fake_1", "ouvrir", Map.of("url", "http://localhost:4200")),
            ClaudeReply.toolUse("fake_2", "lire_page", Map.of()),
            ClaudeReply.toolUse("fake_3", "remplir",
                    Map.of("selecteur", "[data-testid=\"search\"]", "texte", "casque")),
            ClaudeReply.toolUse("fake_4", "lire_page", Map.of()),
            ClaudeReply.endTurn("Réponse simulée : la recherche « casque » a été saisie dans le catalogue."));

    private final List<ClaudeReply> scenario;

    public FakeClaudeClient() {
        this(DEMO_SCENARIO);
    }

    public FakeClaudeClient(List<ClaudeReply> scenario) {
        this.scenario = List.copyOf(scenario);
    }

    @Override
    public String ask(String prompt) {
        return "Réponse simulée à : " + prompt;
    }

    // Le tour est déduit de l'historique : le client reste sans état et sert plusieurs requêtes.
    @Override
    public ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        long turn = messages.stream().filter(message -> "assistant".equals(message.get("role"))).count();
        return turn < scenario.size()
                ? scenario.get((int) turn)
                : ClaudeReply.endTurn("Fin du scénario simulé.");
    }

    @Override
    public String mode() {
        return "fake";
    }
}
