package dev.abakar.agenttest.history;

import java.time.Instant;

/** Une ligne de l'historique : assez pour lister les parcours sans charger leurs bugs. */
public record RunSummary(
        long id,
        String site,
        String instruction,
        String endedBy,
        double costUsd,
        int actionCount,
        int bugCount,
        Instant createdAt) {
}
