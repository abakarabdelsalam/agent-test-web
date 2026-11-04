package dev.abakar.agenttest.agent;

import dev.abakar.agenttest.claude.ClaudeReply.Usage;

/** Prix du modèle en dollars par million de jetons, pour estimer le coût d'un parcours. */
public record Pricing(double inputPerMillion, double outputPerMillion) {

    public double cost(Usage usage) {
        return (usage.inputTokens() * inputPerMillion + usage.outputTokens() * outputPerMillion) / 1_000_000;
    }
}
