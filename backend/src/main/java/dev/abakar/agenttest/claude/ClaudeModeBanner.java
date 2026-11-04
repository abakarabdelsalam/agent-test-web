package dev.abakar.agenttest.claude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Annonce au démarrage si le backend appelle vraiment Claude, pour éviter toute confusion sur les coûts. */
@Component
public class ClaudeModeBanner {

    private static final Logger LOG = LoggerFactory.getLogger(ClaudeModeBanner.class);

    private final ClaudeClient claude;
    private final String model;

    public ClaudeModeBanner(ClaudeClient claude, @Value("${agent.claude.model}") String model) {
        this.claude = claude;
        this.model = model;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void announce() {
        LOG.info(message(claude.mode(), model));
    }

    static String message(String mode, String model) {
        return "real".equals(mode)
                ? "Mode Claude : réel (" + model + "), chaque appel consomme du crédit API."
                : "Mode Claude : simulé, aucun appel à l'API.";
    }
}
