package dev.abakar.agenttest.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.browser.BrowserSession;
import dev.abakar.agenttest.claude.ClaudeReply.ToolCall;
import dev.abakar.agenttest.report.BugReport;
import dev.abakar.agenttest.report.InvalidReportException;
import dev.abakar.agenttest.report.ReportValidator;
import dev.abakar.agenttest.report.RunReport;
import dev.abakar.agenttest.report.Severity;

/** Traduit chaque outil demandé par Claude en action sur le navigateur, avec les garde-fous. */
public class BrowserTools {

    private static final int MAX_ERROR_CHARS = 300;

    /** Le menu présenté à Claude. Tout outil absent de cette liste est refusé. */
    public static final List<Map<String, Object>> DEFINITIONS = List.of(
            tool("ouvrir", "Ouvre une URL du site testé.",
                    Map.of("url", text("URL complète, par exemple http://localhost:4200/panier")), List.of("url")),
            tool("lire_page", "Renvoie le texte visible de la page et ses éléments utilisables avec leur sélecteur.",
                    Map.of(), List.of()),
            tool("cliquer", "Clique sur le premier élément qui correspond au sélecteur Playwright.",
                    Map.of("selecteur", text("Sélecteur donné par lire_page")), List.of("selecteur")),
            tool("remplir", "Remplace le contenu d'un champ de saisie.",
                    Map.of("selecteur", text("Sélecteur donné par lire_page"), "texte", text("Texte à saisir")),
                    List.of("selecteur", "texte")),
            tool("choisir", "Choisit une option dans une liste déroulante (select), par sa valeur ou son libellé.",
                    Map.of("selecteur", text("Sélecteur donné par lire_page"), "option", text("Valeur ou libellé")),
                    List.of("selecteur", "option")),
            tool("capture", "Prend une capture d'écran de la page.", Map.of(), List.of()),
            tool("signaler_bug", "Signale un écart prouvé entre la page et une exigence SPEC-xx.",
                    Map.of("spec", text("Exigence contredite, par exemple SPEC-01"),
                            "title", text("Titre court du bug"),
                            "expected", text("Comportement attendu"),
                            "actual", text("Comportement constaté"),
                            "severity", Map.of("type", "string", "enum", List.of("MINEURE", "MAJEURE", "CRITIQUE")),
                            "steps", Map.of("type", "array", "items", text("Étape pour reproduire"))),
                    List.of("spec", "title", "expected", "actual", "severity", "steps")));

    private final BrowserSession session;
    private final AllowedSites allowedSites;
    private final ReportValidator validator;
    private final List<BugReport> bugs = new ArrayList<>();

    public BrowserTools(BrowserSession session, AllowedSites allowedSites, ReportValidator validator) {
        this.session = session;
        this.allowedSites = allowedSites;
        this.validator = validator;
    }

    /** Exécute une demande. Une erreur devient un résultat lisible par Claude, jamais une exception. */
    public ToolResult execute(ToolCall call) {
        try {
            return switch (call.name()) {
                case "ouvrir" -> open(required(call, "url"));
                case "lire_page" -> ToolResult.ok(session.readPage());
                case "cliquer" -> {
                    session.click(required(call, "selecteur"));
                    yield afterAction("Clic effectué. URL actuelle : " + session.currentUrl());
                }
                case "remplir" -> {
                    session.fill(required(call, "selecteur"), required(call, "texte"));
                    yield afterAction("Champ rempli.");
                }
                case "choisir" -> {
                    session.select(required(call, "selecteur"), required(call, "option"));
                    yield afterAction("Option choisie.");
                }
                case "capture" -> ToolResult.image(session.capture());
                case "signaler_bug" -> report(call);
                default -> ToolResult.error("Outil inconnu : " + call.name());
            };
        } catch (RuntimeException e) {
            return ToolResult.error(shorten(e.getMessage()));
        }
    }

    public List<BugReport> bugs() {
        return List.copyOf(bugs);
    }

    // L'état de la page part avec le résultat : Claude n'a pas besoin de gaspiller une action sur lire_page.
    private ToolResult afterAction(String message) {
        return ToolResult.ok(message + "\n\n" + session.readPage());
    }

    private ToolResult open(String url) {
        if (!allowedSites.allows(url)) {
            return ToolResult.error("URL refusée : seuls les sites autorisés peuvent être ouverts.");
        }
        session.open(url);
        return ToolResult.ok("Page ouverte : " + session.currentUrl());
    }

    private ToolResult report(ToolCall call) {
        Object steps = call.input().get("steps");
        BugReport bug = new BugReport(
                required(call, "spec"), required(call, "title"), required(call, "expected"),
                required(call, "actual"), Severity.valueOf(required(call, "severity")),
                steps instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of());
        try {
            validator.validate(new RunReport(session.currentUrl(), List.of(bug)));
        } catch (InvalidReportException e) {
            return ToolResult.error(e.getMessage());
        }
        bugs.add(bug);
        return ToolResult.ok("Bug enregistré : " + bug.spec() + " " + bug.title());
    }

    private static String required(ToolCall call, String name) {
        if (!(call.input().get(name) instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException("Paramètre manquant : " + name);
        }
        return value;
    }

    static String shorten(String message) {
        if (message == null || message.isBlank()) {
            return "Erreur inconnue";
        }
        // Playwright écrit "Error {" en première ligne : la vraie cause est sur la ligne message='...'.
        String line = message.lines()
                .map(String::strip)
                .filter(candidate -> candidate.startsWith("message='"))
                .map(candidate -> candidate.substring("message='".length()).replaceFirst("'$", ""))
                .findFirst()
                .orElse(message.lines().findFirst().orElse("Erreur inconnue"));
        return line.length() > MAX_ERROR_CHARS ? line.substring(0, MAX_ERROR_CHARS) + "..." : line;
    }

    private static Map<String, Object> text(String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> tool(String name, String description,
            Map<String, Object> properties, List<String> required) {
        return Map.of(
                "name", name,
                "description", description,
                "input_schema", Map.of(
                        "type", "object",
                        "properties", properties,
                        "required", required,
                        "additionalProperties", false));
    }
}
