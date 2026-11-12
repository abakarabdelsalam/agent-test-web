package dev.abakar.agenttest.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                () -> new AnthropicClaudeClient("", "claude-haiku-4-5", 500, 4096));
    }

    @Test
    void fakeClientAnswersWithoutNetwork() {
        assertEquals("Réponse simulée à : test", new FakeClaudeClient().ask("test"));
    }

    @Test
    void buildsToolRequest() {
        Map<String, Object> request = AnthropicClaudeClient.toolRequest("claude-haiku-4-5", 4096, "Système",
                List.of(Map.of("role", "user", "content", "Bonjour")), List.of(Map.of("name", "ouvrir")));

        assertEquals("claude-haiku-4-5", request.get("model"));
        assertEquals(4096, request.get("max_tokens"));
        assertEquals("Système", request.get("system"));
        assertEquals(1, ((List<?>) request.get("tools")).size());
    }

    @Test
    void omitsToolsWhenThereAreNone() {
        Map<String, Object> request = AnthropicClaudeClient.toolRequest("claude-haiku-4-5", 500, "Système",
                List.of(Map.of("role", "user", "content", "Bonjour")), List.of());

        assertFalse(request.containsKey("tools"));
    }

    @Test
    void readsToolUseResponse() {
        Map<String, Object> response = Map.of("stop_reason", "tool_use", "content", List.of(
                Map.of("type", "tool_use", "id", "toolu_1", "name", "lire_page", "input", Map.of())));

        ClaudeReply reply = AnthropicClaudeClient.toReply(response);

        assertEquals("tool_use", reply.stopReason());
        assertEquals("lire_page", reply.toolCalls().getFirst().name());
        assertEquals(ClaudeReply.Usage.NONE, reply.usage());
    }

    @Test
    void readsTokenUsage() {
        Map<String, Object> response = Map.of("stop_reason", "end_turn", "content", List.of(),
                "usage", Map.of("input_tokens", 1200, "output_tokens", 85));

        assertEquals(new ClaudeReply.Usage(1200, 85), AnthropicClaudeClient.toReply(response).usage());
    }

    @Test
    void refusesResponseWithoutStopReason() {
        assertThrows(IllegalStateException.class,
                () -> AnthropicClaudeClient.toReply(Map.of("content", List.of())));
    }

    @Test
    void fakeClientReplaysScenarioByTurn() {
        FakeClaudeClient fake = new FakeClaudeClient();
        List<Map<String, Object>> oneAssistantTurn = List.of(
                Map.of("role", "user", "content", "x"), Map.of("role", "assistant", "content", List.of()));

        assertEquals("ouvrir", fake.converse("", List.of(), List.of()).toolCalls().getFirst().name());
        assertEquals("lire_page", fake.converse("", oneAssistantTurn, List.of()).toolCalls().getFirst().name());
    }
}
