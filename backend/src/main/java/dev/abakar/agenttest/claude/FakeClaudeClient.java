package dev.abakar.agenttest.claude;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Réponse simulée, pour développer sans clé API et sans frais. */
@Component
@ConditionalOnProperty(name = "agent.claude.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClaudeClient implements ClaudeClient {

    @Override
    public String ask(String prompt) {
        return "Réponse simulée à : " + prompt;
    }

    @Override
    public String mode() {
        return "fake";
    }
}
