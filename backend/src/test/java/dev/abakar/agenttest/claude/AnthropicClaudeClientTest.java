package dev.abakar.agenttest.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AnthropicClaudeClientTest {

    @Test
    void keepsOnlyTextBlocks() {
        Map<String, Object> response = Map.of("content", List.of(
                Map.of("type", "text", "text", "Bonjour "),
                Map.of("type", "tool_use", "name", "cliquer"),
                Map.of("type", "text", "text", "Abdelsalam")));

        assertEquals("Bonjour Abdelsalam", AnthropicClaudeClient.extractText(response));
    }

    @Test
    void refusesUnexpectedResponse() {
        assertThrows(IllegalStateException.class, () -> AnthropicClaudeClient.extractText(Map.of()));
    }

    @Test
    void refusesMissingApiKey() {
        assertThrows(IllegalStateException.class,
                () -> new AnthropicClaudeClient("", "claude-haiku-5-5", 500));
    }

    @Test
    void fakeClientAnswersWithoutNetwork() {
        assertEquals("Réponse simulée à : test", new FakeClaudeClient().ask("test"));
    }
}
