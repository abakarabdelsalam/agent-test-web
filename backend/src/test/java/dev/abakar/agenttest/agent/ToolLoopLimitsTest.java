package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.claude.FakeClaudeClient;
import org.junit.jupiter.api.Test;

/**
 * Décrit le contrôle des limites que limitReached doit implémenter.
 * Limites de test : 5 actions, 3 minutes, 0,05 $.
 */
class ToolLoopLimitsTest {

    private static final String URL = "http://localhost:4200";

    private final ToolLoop loop = TestLoops.loop(new FakeClaudeClient());
    private final RecordingBrowserSession session = new RecordingBrowserSession();

    // --- La méthode seule ---

    @Test
    void continuesWhileEveryLimitIsRespected() {
        assertEquals(Optional.empty(), loop.limitReached(4, Duration.ofMinutes(2), 0.04));
    }

    @Test
    void stopsWhenActionLimitIsReached() {
        assertEquals(Optional.of("action_limit"), loop.limitReached(5, Duration.ZERO, 0));
    }

    @Test
    void stopsWhenDurationLimitIsReached() {
        assertEquals(Optional.of("time_limit"), loop.limitReached(0, Duration.ofMinutes(3), 0));
    }

    @Test
    void stopsWhenBudgetLimitIsReached() {
        assertEquals(Optional.of("budget_limit"), loop.limitReached(0, Duration.ZERO, 0.05));
    }

    // --- La méthode dans la boucle complète ---

    @Test
    void stopsAfterFiveActions() {
        AgentRun run = TestLoops.loop(new FakeClaudeClient(endlessReads(10))).run(session, URL, "Lis la page");

        assertEquals("action_limit", run.endedBy());
        assertEquals(5, run.actions().size());
    }

    @Test
    void countsEachActionOfAParallelRequest() {
        Map<String, Object> read = Map.of("type", "tool_use", "id", "t", "name", "lire_page", "input", Map.of());
        ClaudeReply sixAtOnce = new ClaudeReply("tool_use", List.of(read, read, read, read, read, read));

        AgentRun run = TestLoops.loop(new FakeClaudeClient(List.of(sixAtOnce))).run(session, URL, "Lis la page");

        assertEquals("action_limit", run.endedBy());
        assertEquals(5, session.calls.size());
    }

    @Test
    void stopsWhenTheAgentIsTooSlow() {
        AgentRun run = TestLoops.loop(new FakeClaudeClient(endlessReads(10)), TestLoops.LIMITS,
                TestLoops.ticking(Duration.ofMinutes(1)), line -> { }).run(session, URL, "Lis la page");

        assertEquals("time_limit", run.endedBy());
    }

    @Test
    void stopsBeforeTheNextCallWhenBudgetIsSpent() {
        // Chaque appel coûte 0,02 $. Le 3e appel porte le total à 0,06 $ : son action n'est pas exécutée.
        ClaudeReply.Usage expensive = new ClaudeReply.Usage(200_000, 0);
        List<ClaudeReply> replies = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            replies.add(new ClaudeReply("tool_use",
                    ClaudeReply.toolUse("t" + i, "lire_page", Map.of()).content(), expensive));
        }

        AgentRun run = TestLoops.loop(new FakeClaudeClient(replies)).run(session, URL, "Lis la page");

        assertEquals("budget_limit", run.endedBy());
        assertEquals(2, run.actions().size());
    }

    private static List<ClaudeReply> endlessReads(int count) {
        List<ClaudeReply> replies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            replies.add(ClaudeReply.toolUse("toolu_" + i, "lire_page", Map.of()));
        }
        return replies;
    }
}
