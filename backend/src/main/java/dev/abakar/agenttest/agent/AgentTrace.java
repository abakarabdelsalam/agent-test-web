package dev.abakar.agenttest.agent;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Journal lisible du parcours, affiché dans la console seulement en mode observation. */
@Component
public class AgentTrace {

    private static final Logger LOG = LoggerFactory.getLogger(AgentTrace.class);
    private static final int MAX_CHARS = 160;
    // Filet de sécurité : une clé qui se glisserait dans un texte n'est jamais affichée.
    private static final Pattern API_KEY = Pattern.compile("sk-ant-[A-Za-z0-9_-]+");

    private final boolean enabled;
    private final Consumer<String> out;

    @Autowired
    public AgentTrace(@Value("${agent.headed}") boolean enabled) {
        this(enabled, LOG::info);
    }

    AgentTrace(boolean enabled, Consumer<String> out) {
        this.enabled = enabled;
        this.out = out;
    }

    public void start(String url, String instruction) {
        print("Début du parcours sur " + url + " : " + instruction);
    }

    /** Texte que le modèle écrit avant de demander une action. */
    public void thought(String text) {
        if (text != null && !text.isBlank()) {
            print("Le modèle dit : " + text);
        }
    }

    public void action(int number, int max, String tool, Map<String, Object> input, ToolResult result) {
        print("Action " + number + "/" + max + " : " + tool + " " + input
                + " -> " + (result.error() ? "ERREUR " : "") + result.text());
    }

    public void verdict(String spec, boolean confirmed, String reason) {
        print("Vérification " + spec + " : " + (confirmed ? "confirmé" : "rejeté") + " (" + summarize(reason) + ")");
    }

    public void stop(String endedBy, int actions, double costUsd) {
        print("Arrêt après " + actions + " action(s) : " + reason(endedBy)
                + String.format(Locale.FRANCE, " (coût estimé : %.4f $)", costUsd));
    }

    static String reason(String endedBy) {
        return switch (endedBy) {
            case "end_turn" -> "le modèle a terminé";
            case "action_limit" -> "limite d'actions atteinte";
            case "time_limit" -> "limite de durée atteinte";
            case "budget_limit" -> "limite de budget atteinte";
            case "api_error" -> "erreur de l'API";
            case "max_tokens" -> "réponse coupée par la limite de jetons";
            case "refusal" -> "le modèle a refusé la demande";
            default -> endedBy;
        };
    }

    private void print(String line) {
        if (enabled) {
            out.accept(summarize(line));
        }
    }

    static String summarize(String line) {
        String oneLine = API_KEY.matcher(line).replaceAll("[clé masquée]").replaceAll("\\s+", " ").strip();
        return oneLine.length() > MAX_CHARS ? oneLine.substring(0, MAX_CHARS) + "..." : oneLine;
    }
}
