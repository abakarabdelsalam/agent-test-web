package dev.abakar.agenttest.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.List;

import dev.abakar.agenttest.agent.AgentRun;
import dev.abakar.agenttest.report.BugReport;
import dev.abakar.agenttest.report.RunReport;
import dev.abakar.agenttest.report.Severity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// Base H2 en mémoire : le test n'a pas besoin de Docker ni de PostgreSQL.
@DataJpaTest
class RunHistoryTest {

    @Autowired
    private RunRepository runs;

    @Autowired
    private BugRepository bugs;

    private RunHistory history() {
        return new RunHistory(runs, bugs, Clock.systemUTC());
    }

    private static AgentRun runWithOneBug() {
        BugReport bug = new BugReport("SPEC-01", "Recherche sensible à la casse", "Doit ignorer la casse",
                "« casque » ne trouve rien", Severity.MAJEURE, List.of("Saisir casque", "Lire le résultat"));
        return new AgentRun("Cherche casque", "end_turn", "Un bug trouvé.", 0.002, List.of(),
                new RunReport("http://localhost:4200", List.of(bug)));
    }

    @Test
    void keepsEachRunWithItsBugsAwaitingValidation() {
        RunHistory history = history();
        history.record("http://localhost:4200", runWithOneBug());

        RunSummary summary = history.recent().getFirst();
        assertEquals(1, summary.bugCount());
        assertEquals("end_turn", summary.endedBy());

        RunDetail detail = history.find(summary.id()).orElseThrow();
        assertEquals(ValidationStatus.EN_ATTENTE, detail.bugs().getFirst().status());
        assertEquals(List.of("Saisir casque", "Lire le résultat"), detail.bugs().getFirst().steps());
    }

    @Test
    void recordsValidationDecisionOnABug() {
        RunHistory history = history();
        history.record("http://localhost:4200", runWithOneBug());
        long runId = history.recent().getFirst().id();
        long bugId = history.find(runId).orElseThrow().bugs().getFirst().id();

        history.validate(bugId, ValidationStatus.VALIDE);

        assertEquals(ValidationStatus.VALIDE, history.find(runId).orElseThrow().bugs().getFirst().status());
    }

    @Test
    void remembersOnlyValidatedBugsOfTheSameSite() {
        RunHistory history = history();
        history.record("http://localhost:4200", runWithOneBug());
        long bugId = history.find(history.recent().getFirst().id()).orElseThrow().bugs().getFirst().id();

        assertTrue(history.validatedFor("http://localhost:4200").isEmpty());

        history.validate(bugId, ValidationStatus.VALIDE);

        assertEquals(List.of("SPEC-01 : Recherche sensible à la casse"),
                history.validatedFor("http://localhost:4200"));
        assertTrue(history.validatedFor("http://autre-site").isEmpty());
    }

    @Test
    void reportsUnknownRunAndBugAsAbsent() {
        RunHistory history = history();

        assertFalse(history.find(999).isPresent());
        assertFalse(history.validate(999, ValidationStatus.REJETE).isPresent());
        assertTrue(history.recent().isEmpty());
    }
}
