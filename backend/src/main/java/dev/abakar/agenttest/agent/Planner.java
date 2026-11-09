package dev.abakar.agenttest.agent;

import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.claude.ClaudeReply.Usage;
import org.springframework.stereotype.Component;

/**
 * Premier agent : il découpe la consigne en étapes courtes, avant que le testeur n'agisse.
 * Pas d'outil, pas de navigateur : seulement une réponse texte.
 */
@Component
public class Planner {

    static final String SYSTEM = """
            Tu prépares un test de site web. Tu reçois une consigne : découpe-la en 2 à 5 étapes courtes, une par ligne,
            chacune commençant par « - ». Ne donne aucune autre explication.
            """;

    private static final int MAX_STEPS = 5;

    public record Plan(List<String> steps, Usage usage) {
    }

    private final ClaudeClient claude;

    public Planner(ClaudeClient claude) {
        this.claude = claude;
    }

    public Plan plan(String instruction) {
        ClaudeReply reply = claude.converse(SYSTEM, List.of(Map.of("role", "user", "content", instruction)), List.of());
        List<String> steps = reply.text().lines()
                .map(String::strip)
                .filter(line -> line.startsWith("- "))
                .map(line -> line.substring(2).strip())
                .limit(MAX_STEPS)
                .toList();
        return new Plan(steps, reply.usage());
    }
}
