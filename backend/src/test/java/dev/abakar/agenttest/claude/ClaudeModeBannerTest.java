package dev.abakar.agenttest.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClaudeModeBannerTest {

    @Test
    void announcesFakeModeWithoutCost() {
        assertEquals("Mode Claude : simulé, aucun appel à l'API.",
                ClaudeModeBanner.message(new FakeClaudeClient().mode(), "claude-haiku-4-5"));
    }

    @Test
    void warnsThatRealModeCostsMoney() {
        assertEquals("Mode Claude : réel (claude-haiku-4-5), chaque appel consomme du crédit API.",
                ClaudeModeBanner.message("real", "claude-haiku-4-5"));
    }
}
