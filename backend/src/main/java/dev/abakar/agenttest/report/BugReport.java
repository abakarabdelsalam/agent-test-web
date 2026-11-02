package dev.abakar.agenttest.report;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Un bug prouvé : l'exigence contredite, l'écart constaté et les étapes pour le reproduire. */
public record BugReport(
        @NotBlank @Pattern(regexp = "SPEC-\\d{2}") String spec,
        @NotBlank String title,
        @NotBlank String expected,
        @NotBlank String actual,
        @NotNull Severity severity,
        @NotEmpty List<@NotBlank String> steps) {
}
