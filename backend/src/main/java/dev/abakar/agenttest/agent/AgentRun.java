package dev.abakar.agenttest.agent;

import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.report.RunReport;

/**
 * Compte rendu d'un parcours de l'agent.
 * endedBy : end_turn (Claude a fini), action_limit, time_limit, budget_limit, api_error, max_tokens ou refusal.
 * costUsd : coût estimé à partir des jetons indiqués par l'API et des prix configurés.
 */
public record AgentRun(
        String instruction,
        String endedBy,
        String finalText,
        double costUsd,
        List<Action> actions,
        RunReport report) {

    /** Une action exécutée, avec un résumé de son résultat. */
    public record Action(String tool, Map<String, Object> input, String result, boolean error) {
    }
}
