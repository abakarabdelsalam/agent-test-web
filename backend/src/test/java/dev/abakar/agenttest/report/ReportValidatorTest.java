package dev.abakar.agenttest.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class ReportValidatorTest {

    private final ReportValidator validator =
            new ReportValidator(Validation.buildDefaultValidatorFactory().getValidator());

    private RunReport reportWith(BugReport bug) {
        return new RunReport("http://localhost:4200", List.of(bug));
    }

    @Test
    void acceptsCompleteReport() {
        BugReport bug = new BugReport("SPEC-11", "Remise fausse", "10 %", "20 %",
                Severity.CRITIQUE, List.of("Saisir le code"));

        assertDoesNotThrow(() -> validator.validate(reportWith(bug)));
    }

    @Test
    void refusesUnknownSpecFormat() {
        BugReport bug = new BugReport("spec 11", "Remise fausse", "10 %", "20 %",
                Severity.CRITIQUE, List.of("Saisir le code"));

        assertThrows(InvalidReportException.class, () -> validator.validate(reportWith(bug)));
    }

    @Test
    void refusesBugWithoutSteps() {
        BugReport bug = new BugReport("SPEC-11", "Remise fausse", "10 %", "20 %",
                Severity.CRITIQUE, List.of());

        assertThrows(InvalidReportException.class, () -> validator.validate(reportWith(bug)));
    }
}
