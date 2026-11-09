package dev.abakar.agenttest.agent;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.function.Consumer;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.claude.ClaudeClient;
import dev.abakar.agenttest.claude.ClaudeReply;
import dev.abakar.agenttest.report.ReportValidator;
import jakarta.validation.Validation;

/** Construit une boucle de test, sans Spring, avec des limites et une horloge choisies. */
public final class TestLoops {

    public static final AgentLimits LIMITS = new AgentLimits(5, Duration.ofMinutes(3), 0.05);
    public static final Pricing HAIKU = new Pricing(0.10, 0.50);

    // Le planificateur ne doit ni consommer les réponses prévues ni coûter : il répond une fois, sans texte ni coût.
    private static final ClaudeClient SILENT = new ClaudeClient() {
        @Override
        public ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
            return ClaudeReply.endTurn("");
        }

        @Override
        public String ask(String prompt) {
            return "";
        }

        @Override
        public String mode() {
            return "fake";
        }
    };

    private TestLoops() {
    }

    public static ToolLoop loop(ClaudeClient claude, AgentLimits limits, Clock clock, Consumer<String> console) {
        return loop(claude, limits, clock, console, KnownFindings.NONE);
    }

    public static ToolLoop loop(ClaudeClient claude, AgentLimits limits, Clock clock, Consumer<String> console,
            KnownFindings known) {
        return new ToolLoop(claude, new AllowedSites(Set.of("localhost")),
                new ReportValidator(Validation.buildDefaultValidatorFactory().getValidator()),
                new AgentTrace(true, console), limits, HAIKU, new Specifications(Path.of("absent.md")), clock,
                known, new BugVerifier(claude), new Planner(SILENT));
    }

    public static ToolLoop loop(ClaudeClient claude) {
        return loop(claude, LIMITS, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), line -> { });
    }

    /** Horloge qui avance d'un pas à chaque lecture : simule un agent lent. */
    public static Clock ticking(Duration step) {
        return new Clock() {
            private Instant now = Instant.EPOCH;

            @Override
            public Instant instant() {
                Instant current = now;
                now = now.plus(step);
                return current;
            }

            @Override
            public java.time.ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }
        };
    }
}
