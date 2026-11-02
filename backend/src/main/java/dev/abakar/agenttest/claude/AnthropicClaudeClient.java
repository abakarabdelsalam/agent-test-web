package dev.abakar.agenttest.claude;

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

    public AnthropicClaudeClient(
            @Value("${agent.claude.api-key}") String apiKey,
            @Value("${agent.claude.model}") String model,
            @Value("${agent.claude.max-tokens}") int maxTokens) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY est requise quand CLAUDE_MODE=real");
        }
        this.model = model;
        this.maxTokens = maxTokens;
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

        Map<?, ?> response = restClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    @Override
    public String mode() {
        return "real";
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
}
