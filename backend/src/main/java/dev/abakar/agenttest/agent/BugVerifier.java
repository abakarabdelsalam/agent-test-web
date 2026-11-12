package dev.abakar.agenttest.agent;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.claude.ClaudeReply.Usage;
import dev.abakar.agenttest.report.BugReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deuxième agent : il revérifie un bug signalé par le testeur, à partir des observations, sans navigateur.
 * Un bug entre dans le rapport seulement s'il est confirmé.
 */
@Component
public class BugVerifier {

    static final String SYSTEM = """
            Tu es un vérificateur de bugs. Tu reçois un bug signalé par un agent de test et les observations qui le justifient.
            Réponds CONFIRMÉ si les observations prouvent l'écart avec le comportement attendu, sinon REJETÉ.
            Commence ta réponse par ce seul mot, sans mise en forme, puis donne une raison en une phrase.
            """;

    public record Verdict(boolean confirmed, String reason, Usage usage) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(BugVerifier.class);

    private final ClaudeClient claude;

    public BugVerifier(ClaudeClient claude) {
        this.claude = claude;
    }

    public Verdict verify(BugReport bug, String evidence) {
        String question = bug.spec() + " " + bug.title() + "\n"
                + "Attendu : " + bug.expected() + "\n"
                + "Constaté : " + bug.actual() + "\n\n"
                + "Observations :\n" + evidence;
        ClaudeReply reply = claude.converse(SYSTEM, List.of(Map.of("role", "user", "content", question)), List.of());
        String text = reply.text().strip();
        boolean confirmed = isConfirmed(text);
        // Toujours journalisé : un bug rejeté à tort doit pouvoir être expliqué.
        LOG.info("Vérification {} : {} ({})", bug.spec(), confirmed ? "confirmé" : "rejeté", text);
        return new Verdict(confirmed, text, reply.usage());
    }

    // Le modèle peut écrire **CONFIRMÉ**, Confirmé ou un É décomposé : seul le premier mot compte.
    static boolean isConfirmed(String text) {
        String plain = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceFirst("^[^A-Z]+", "");
        return plain.startsWith("CONFIRME");
    }
}
