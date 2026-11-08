package dev.abakar.agenttest.history;

import dev.abakar.agenttest.agent.AgentRun;

/** Ce que l'API appelle après chaque parcours, pour le garder. Les tests peuvent le remplacer. */
public interface RunRecorder {

    void record(String site, AgentRun run);
}
