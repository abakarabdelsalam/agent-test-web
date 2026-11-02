package dev.abakar.agenttest.agent;

import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.report.RunReport;

/**
 * Compte rendu d'un parcours de l'agent.
 * endedBy : end_turn (Claude a fini), action_limit, max_tokens ou refusal.
 */
public record AgentRun(
        String instruction,
        String endedBy,
        String finalText,
        List<Action> actions,
        RunReport report) {

    /** Une action exécutée, avec un résumé de son résultat. */
    public record Action(String tool, Map<String, Object> input, String result, boolean error) {
    }
}
