package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.claude.FakeClaudeClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class ToolLoopTest {

    private static final String URL = "http://localhost:4200";

    private final RecordingBrowserSession session = new RecordingBrowserSession();
    private final List<String> console = new ArrayList<>();

    private ToolLoop loopWith(ClaudeClient claude) {
        return TestLoops.loop(claude, TestLoops.LIMITS, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), console::add);
    }

    @Test
    void printsEachActionAndTheReasonToStop() {
        loopWith(new FakeClaudeClient()).run(session, URL, "Cherche casque");

        assertTrue(console.getFirst().startsWith("Début du parcours sur " + URL));
        assertTrue(console.stream().anyMatch(line -> line.startsWith("Action 1/5 : ouvrir {url=" + URL + "}")));
        assertTrue(console.stream().anyMatch(line -> line.startsWith("Action 3/5 : remplir")));
        assertEquals("Arrêt après 4 action(s) : le modèle a terminé (coût estimé : 0,0000 $)", console.getLast());
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
    void doesNotRunToolsWhenClaudeRefuses() {
        ClaudeReply refusal = new ClaudeReply("refusal", List.of(
                Map.of("type", "tool_use", "id", "t", "name", "ouvrir", "input", Map.of("url", URL))));

        AgentRun run = loopWith(new FakeClaudeClient(List.of(refusal))).run(session, URL, "Ouvre");

        assertEquals("refusal", run.endedBy());
        assertTrue(session.calls.isEmpty());
    }

    @Test
    void addsUpTheCostOfEachCall() {
        ClaudeReply.Usage usage = new ClaudeReply.Usage(10_000, 1_000);
        List<ClaudeReply> replies = List.of(
                new ClaudeReply("tool_use", ClaudeReply.toolUse("t1", "lire_page", Map.of()).content(), usage),
                new ClaudeReply("end_turn", ClaudeReply.endTurn("Fini.").content(), usage));

        AgentRun run = loopWith(new FakeClaudeClient(replies)).run(session, URL, "Lis");

        // 2 appels × (10 000 × 0,10 $ + 1 000 × 0,50 $) / 1 000 000 = 0,003 $
        assertEquals(0.003, run.costUsd(), 1e-9);
    }

    @Test
    void stopsCleanlyWhenTheApiFails() {
        ClaudeClient failing = new FakeClaudeClient() {
            @Override
            public ClaudeReply converse(String system, List<Map<String, Object>> messages,
                    List<Map<String, Object>> tools) {
                throw new RestClientException("401 Unauthorized");
            }
        };

        AgentRun run = loopWith(failing).run(session, URL, "Ouvre");

        assertEquals("api_error", run.endedBy());
        assertEquals("401 Unauthorized", run.finalText());
        assertTrue(session.calls.isEmpty());
    }

    @Test
    void givesSpecificationsAndLimitToTheModel() {
        List<String> systems = new ArrayList<>();
        ClaudeClient recording = new FakeClaudeClient() {
            @Override
            public ClaudeReply converse(String system, List<Map<String, Object>> messages,
                    List<Map<String, Object>> tools) {
                systems.add(system);
                return ClaudeReply.endTurn("Fini.");
            }
        };

        loopWith(recording).run(session, URL, "Ouvre");

        assertTrue(systems.getFirst().contains("au plus 5 actions"));
        assertTrue(systems.getFirst().contains("<specifications>"));
    }

    @Test
    void sendsBackFullHistoryWithMatchingIds() {
        List<List<Map<String, Object>>> histories = new ArrayList<>();
        ClaudeClient recording = new FakeClaudeClient(List.of(
                ClaudeReply.toolUse("toolu_open", "ouvrir", Map.of("url", URL)), ClaudeReply.endTurn("Fait."))) {
            @Override
            public ClaudeReply converse(String system, List<Map<String, Object>> messages,
                    List<Map<String, Object>> tools) {
                histories.add(List.copyOf(messages));
                return super.converse(system, messages, tools);
            }
        };

        loopWith(recording).run(session, URL, "Ouvre le site");

        List<Map<String, Object>> secondCall = histories.get(1);
        assertEquals(List.of("user", "assistant", "user"),
                secondCall.stream().map(message -> message.get("role")).toList());
        Map<?, ?> result = (Map<?, ?>) ((List<?>) secondCall.get(2).get("content")).getFirst();
        assertEquals("toolu_open", result.get("tool_use_id"));
    }
}
