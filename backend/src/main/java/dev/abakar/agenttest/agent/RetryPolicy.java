package dev.abakar.agenttest.agent;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.web.client.RestClientResponseException;

/**
 * Reprend un appel à l'API quand l'erreur est passagère : trop de requêtes (429) ou serveur indisponible (5xx).
 * Une erreur qui vient de notre requête (400, clé refusée) n'est jamais reprise : elle recommencerait à l'identique.
 */
public record RetryPolicy(int attempts, Duration pause) {

    public <T> T run(Supplier<T> call) {
        for (int attempt = 1; ; attempt++) {
            try {
                return call.get();
            } catch (RestClientResponseException e) {
                if (attempt >= attempts || !transientError(e)) {
                    throw e;
                }
                sleep();
            }
        }
    }

    static boolean transientError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        return status == 429 || status >= 500;
    }

    private void sleep() {
        try {
            Thread.sleep(pause.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Reprise interrompue", e);
        }
    }
}
