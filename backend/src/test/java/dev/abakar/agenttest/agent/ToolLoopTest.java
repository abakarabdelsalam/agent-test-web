package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.claude.FakeClaudeClient;
import dev.abakar.agenttest.report.ReportValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class ToolLoopTest {

    private static final String URL = "http://localhost:4200";

    private final RecordingBrowserSession session = new RecordingBrowserSession();
    private final List<String> console = new ArrayList<>();

    private ToolLoop loopWith(ClaudeClient claude) {
        return new ToolLoop(claude, new AllowedSites(Set.of("localhost")),
                new ReportValidator(Validation.buildDefaultValidatorFactory().getValidator()),
                new AgentTrace(true, console::add));
    }

    @Test
    void printsEachActionAndTheReasonToStop() {
        loopWith(new FakeClaudeClient()).run(session, URL, "Cherche casque");

        assertTrue(console.getFirst().startsWith("Début du parcours sur " + URL));
        assertTrue(console.stream().anyMatch(line -> line.startsWith("Action 1/5 : ouvrir {url=" + URL + "}")));
        assertTrue(console.stream().anyMatch(line -> line.startsWith("Action 3/5 : remplir")));
        assertEquals("Arrêt après 4 action(s) : le modèle a terminé", console.getLast());
    }

    @Test
    void playsDemoScenarioUntilClaudeFinishes() {
        AgentRun run = loopWith(new FakeClaudeClient()).run(session, URL, "Cherche casque");

        assertEquals("end_turn", run.endedBy());
        assertEquals(List.of("ouvrir", "lire_page", "remplir", "lire_page"),
                run.actions().stream().map(AgentRun.Action::tool).toList());
        assertEquals("open " + URL, session.calls.getFirst());
        assertTrue(run.report().bugs().isEmpty());
    }

    @Test
    void stopsAfterFiveActions() {
        List<ClaudeReply> endless = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            endless.add(ClaudeReply.toolUse("toolu_" + i, "lire_page", Map.of()));
        }

        AgentRun run = loopWith(new FakeClaudeClient(endless)).run(session, URL, "Lis la page");

        assertEquals("action_limit", run.endedBy());
        assertEquals(ToolLoop.MAX_ACTIONS, run.actions().size());
        assertEquals(ToolLoop.MAX_ACTIONS, session.calls.size());
    }

    @Test
    void countsEachActionOfAParallelRequest() {
        Map<String, Object> read = Map.of("type", "tool_use", "id", "t", "name", "lire_page", "input", Map.of());
        ClaudeReply sixAtOnce = new ClaudeReply("tool_use", List.of(read, read, read, read, read, read));

        AgentRun run = loopWith(new FakeClaudeClient(List.of(sixAtOnce))).run(session, URL, "Lis la page");

        assertEquals("action_limit", run.endedBy());
        assertEquals(ToolLoop.MAX_ACTIONS, session.calls.size());
    }

    @Test
    void doesNotRunToolsWhenClaudeRefuses() {
        ClaudeReply refusal = new ClaudeReply("refusal", List.of(
                Map.of("type", "tool_use", "id", "t", "name", "ouvrir", "input", Map.of("url", URL))));

        AgentRun run = loopWith(new FakeClaudeClient(List.of(refusal))).run(session, URL, "Ouvre");

        assertEquals("refusal", run.endedBy());
        assertTrue(session.calls.isEmpty());
    }

    @Test
    void sendsBackFullHistoryWithMatchingIds() {
        RecordingClaude claude = new RecordingClaude();

        loopWith(claude).run(session, URL, "Ouvre le site");

        List<Map<String, Object>> secondCall = claude.histories.get(1);
        assertEquals(3, secondCall.size());
        assertEquals(List.of("user", "assistant", "user"),
                secondCall.stream().map(message -> message.get("role")).toList());
        Map<?, ?> result = (Map<?, ?>) ((List<?>) secondCall.get(2).get("content")).getFirst();
        assertEquals("toolu_open", result.get("tool_use_id"));
    }

    /** Client simulé qui garde une copie de l'historique reçu à chaque appel. */
    private static class RecordingClaude extends FakeClaudeClient {

        final List<List<Map<String, Object>>> histories = new ArrayList<>();

        RecordingClaude() {
            super(List.of(ClaudeReply.toolUse("toolu_open", "ouvrir", Map.of("url", URL)),
                    ClaudeReply.endTurn("Fait.")));
        }

        @Override
        public ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
            histories.add(List.copyOf(messages));
            return super.converse(system, messages, tools);
        }
    }
}
