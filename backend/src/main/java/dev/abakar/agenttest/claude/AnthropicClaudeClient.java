package dev.abakar.agenttest.claude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Appelle l'API Messages de Claude. Chaque appel est facturé. */
@Component
@ConditionalOnProperty(name = "agent.claude.mode", havingValue = "real")
public class AnthropicClaudeClient implements ClaudeClient {

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final int toolMaxTokens;

    public AnthropicClaudeClient(
            @Value("${agent.claude.api-key}") String apiKey,
            @Value("${agent.claude.model}") String model,
            @Value("${agent.claude.max-tokens}") int maxTokens,
            @Value("${agent.claude.tool-max-tokens}") int toolMaxTokens) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY est requise quand CLAUDE_MODE=real");
        }
        this.model = model;
        this.maxTokens = maxTokens;
        this.toolMaxTokens = toolMaxTokens;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    public String ask(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", prompt)));

        return extractText(post(request));
    }

    @Override
    public ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        return toReply(post(toolRequest(model, toolMaxTokens, system, messages, tools)));
    }

    @Override
    public String mode() {
        return "real";
    }

    private Map<?, ?> post(Map<String, Object> request) {
        return restClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);
    }

    static Map<String, Object> toolRequest(String model, int maxTokens, String system,
            List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        Map<String, Object> request = new HashMap<>(Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", system,
                "messages", messages));
        // Sans outil, on n'envoie pas le champ : c'est le cas du vérificateur.
        if (!tools.isEmpty()) {
            request.put("tools", tools);
        }
        return request;
    }

    static String extractText(Map<?, ?> response) {
        if (response == null || !(response.get("content") instanceof List<?> blocks)) {
            throw new IllegalStateException("Réponse de l'API Claude inattendue");
        }
        StringBuilder text = new StringBuilder();
        for (Object block : blocks) {
            if (block instanceof Map<?, ?> map && "text".equals(map.get("type"))) {
                text.append(map.get("text"));
            }
        }
        return text.toString();
    }

    static ClaudeReply toReply(Map<?, ?> response) {
        if (response == null || !(response.get("content") instanceof List<?> blocks)
                || !(response.get("stop_reason") instanceof String stopReason)) {
            throw new IllegalStateException("Réponse de l'API Claude inattendue");
        }
        List<Map<String, Object>> content = new ArrayList<>();
        for (Object block : blocks) {
            if (block instanceof Map<?, ?> map) {
                Map<String, Object> copy = new LinkedHashMap<>();
                map.forEach((key, value) -> copy.put(String.valueOf(key), value));
                content.add(copy);
            }
        }
        return new ClaudeReply(stopReason, content, toUsage(response.get("usage")));
    }

    private static ClaudeReply.Usage toUsage(Object usage) {
        if (!(usage instanceof Map<?, ?> map)) {
            return ClaudeReply.Usage.NONE;
        }
        return new ClaudeReply.Usage(tokens(map.get("input_tokens")), tokens(map.get("output_tokens")));
    }

    private static long tokens(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }
}
