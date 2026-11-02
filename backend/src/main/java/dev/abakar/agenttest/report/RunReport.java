package dev.abakar.agenttest.report;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Le rapport complet d'un test. */
public record RunReport(@NotBlank String site, @NotNull List<@Valid BugReport> bugs) {
}
