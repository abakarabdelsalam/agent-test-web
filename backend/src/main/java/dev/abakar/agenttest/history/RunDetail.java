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
        List<BugView> bugs,
        List<ActionView> actions) {

    /** Une action de la trace : ce que l'agent a demandé, et ce qui en est revenu. */
    public record ActionView(int position, String tool, String input, String result, boolean error) {
    }

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
