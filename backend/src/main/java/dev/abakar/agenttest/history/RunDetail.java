package dev.abakar.agenttest.history;

import java.time.Instant;
import java.util.List;

import dev.abakar.agenttest.report.Severity;

/** Un parcours complet, avec chacun de ses bugs et leur statut de validation. */
public record RunDetail(
        long id,
        String site,
        String instruction,
        String endedBy,
        double costUsd,
        int actionCount,
        String finalText,
        Instant createdAt,
        List<BugView> bugs) {

    public record BugView(
            long id,
            String spec,
            String title,
            String expected,
            String actual,
            Severity severity,
            List<String> steps,
            ValidationStatus status) {
    }
}
