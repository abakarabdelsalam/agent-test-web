package dev.abakar.agenttest.report;

import java.util.stream.Collectors;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

/** Refuse tout rapport incomplet, qu'il vienne du code ou, plus tard, du modèle. */
@Component
public class ReportValidator {

    private final Validator validator;

    public ReportValidator(Validator validator) {
        this.validator = validator;
    }

    public RunReport validate(RunReport report) {
        var violations = validator.validate(report);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new InvalidReportException("Rapport invalide : " + details);
        }
        return report;
    }
}
