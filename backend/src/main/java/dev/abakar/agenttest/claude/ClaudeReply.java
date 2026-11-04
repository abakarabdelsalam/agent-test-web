package dev.abakar.agenttest.claude;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Une réponse de Claude lors d'une conversation avec outils.
 * Les blocs sont gardés bruts : on les renvoie tels quels à l'API au tour suivant.
 */
public record ClaudeReply(String stopReason, List<Map<String, Object>> content, Usage usage) {

    public ClaudeReply {
        content = List.copyOf(content);
    }

    public ClaudeReply(String stopReason, List<Map<String, Object>> content) {
        this(stopReason, content, Usage.NONE);
    }

    /** Jetons facturés pour cet appel : ce que le modèle a lu et ce qu'il a écrit. */
    public record Usage(long inputTokens, long outputTokens) {
        public static final Usage NONE = new Usage(0, 0);
    }

    /** Une action demandée par Claude, à exécuter par le code Java. */
    public record ToolCall(String id, String name, Map<String, Object> input) {
    }

    @SuppressWarnings("unchecked")
    public List<ToolCall> toolCalls() {
        return content.stream()
                .filter(block -> "tool_use".equals(block.get("type")))
                .map(block -> new ToolCall((String) block.get("id"), (String) block.get("name"),
                        block.get("input") instanceof Map<?, ?> input ? (Map<String, Object>) input : Map.of()))
                .toList();
    }

    public String text() {
        return content.stream()
                .filter(block -> "text".equals(block.get("type")))
                .map(block -> String.valueOf(block.get("text")))
                .collect(Collectors.joining());
    }

    public static ClaudeReply toolUse(String id, String name, Map<String, Object> input) {
        return new ClaudeReply("tool_use",
                List.of(Map.of("type", "tool_use", "id", id, "name", name, "input", input)));
    }

    public static ClaudeReply endTurn(String text) {
        return new ClaudeReply("end_turn", List.of(Map.of("type", "text", "text", text)));
    }
}
