package dev.abakar.agenttest.agent;

import java.util.List;

/** Les écarts déjà validés par une personne sur un site, pour ne pas les signaler une deuxième fois. */
public interface KnownFindings {

    /** Une ligne par écart, par exemple « SPEC-01 : la recherche ignore la casse ». */
    List<String> validatedFor(String site);

    /** Aucun souvenir : utilisé par les tests et quand l'historique n'est pas disponible. */
    KnownFindings NONE = site -> List.of();
}
