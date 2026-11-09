package dev.abakar.agenttest.agent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Découpe les spécifications en une ligne par exigence, puis retrouve celles qui ressemblent à une question.
 * La ressemblance est simple : le nombre de mots utiles communs. Pas besoin d'API ni de modèle.
 */
public class SpecificationIndex {

    private static final Pattern REQUIREMENT = Pattern.compile("^- (SPEC-\\d{2}) : (.+)$", Pattern.MULTILINE);

    // Mots trop fréquents pour distinguer une exigence d'une autre.
    private static final Set<String> STOP_WORDS = Set.of(
            "les", "des", "une", "est", "pour", "par", "que", "qui", "dans", "sur", "avec",
            "quand", "chaque", "sont", "leur", "leurs", "aux", "donne", "affiche", "doit", "ont");

    public static final Map<String, Object> DEFINITION = Map.of(
            "name", "chercher_specification",
            "description", "Renvoie les exigences SPEC-xx qui ressemblent à la question, par exemple "
                    + "« la recherche ignore-t-elle la casse ? ». Utilise-le avant de signaler un bug.",
            "input_schema", Map.of(
                    "type", "object",
                    "properties", Map.of("question", Map.of("type", "string",
                            "description", "Ce que tu veux vérifier, en français")),
                    "required", List.of("question"),
                    "additionalProperties", false));

    private final List<Requirement> requirements = new ArrayList<>();

    public record Requirement(String id, String text) {
    }

    public SpecificationIndex(String specifications) {
        Matcher matcher = REQUIREMENT.matcher(specifications);
        while (matcher.find()) {
            requirements.add(new Requirement(matcher.group(1), matcher.group(2).strip()));
        }
    }

    /** Les identifiants disponibles, pour que Claude sache ce qu'il peut chercher. */
    public String ids() {
        return requirements.stream().map(Requirement::id).collect(Collectors.joining(", "));
    }

    /** Les exigences les plus proches de la question, de la plus proche à la moins proche. */
    public List<Requirement> search(String question, int limit) {
        Set<String> wanted = terms(question);
        return requirements.stream()
                .map(requirement -> new Scored(requirement, sharedTerms(wanted, requirement.text())))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(Scored::score).reversed()
                        .thenComparing(scored -> scored.requirement().id()))
                .limit(limit)
                .map(Scored::requirement)
                .toList();
    }

    private record Scored(Requirement requirement, int score) {
    }

    private static int sharedTerms(Set<String> wanted, String text) {
        Set<String> found = terms(text);
        return (int) wanted.stream().filter(found::contains).count();
    }

    // Minuscules, sans accents, mots de 3 lettres ou plus, sans mots vides.
    static Set<String> terms(String text) {
        String plain = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return java.util.Arrays.stream(plain.split("[^a-z0-9]+"))
                .filter(word -> word.length() >= 3 && !STOP_WORDS.contains(word))
                .collect(Collectors.toUnmodifiableSet());
    }
}
