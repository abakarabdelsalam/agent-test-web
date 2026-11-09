package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SpecificationIndexTest {

    private static final String TEXT = """
            ## Catalogue

            - SPEC-01 : la recherche par nom ignore les majuscules et les minuscules.
            - SPEC-02 : « Prix croissant » affiche le produit le moins cher en premier.
            - SPEC-03 : le prix d'un produit est le même dans le catalogue et sur sa fiche.
            """;

    @Test
    void listsRequirementIdsInOrder() {
        assertEquals("SPEC-01, SPEC-02, SPEC-03", new SpecificationIndex(TEXT).ids());
    }

    @Test
    void findsTheRequirementThatMatchesTheQuestion() {
        var found = new SpecificationIndex(TEXT).search("la recherche ignore-t-elle les majuscules ?", 3);

        assertEquals("SPEC-01", found.getFirst().id());
    }

    @Test
    void ignoresCaseAndAccents() {
        var found = new SpecificationIndex(TEXT).search("PRIX CROISSANT", 3);

        assertEquals("SPEC-02", found.getFirst().id());
    }

    @Test
    void returnsNothingWhenNoWordMatches() {
        assertTrue(new SpecificationIndex(TEXT).search("livraison gratuite", 3).isEmpty());
    }

    @Test
    void respectsTheLimit() {
        // « prix » figure dans SPEC-02 et SPEC-03 : la limite doit couper à 1.
        assertEquals(1, new SpecificationIndex(TEXT).search("prix", 1).size());
    }

    @Test
    void indexesEveryRequirementOfTheProjectSpecifications() throws IOException {
        // Les tests Maven tournent dans backend/, comme mvn spring-boot:run.
        String text = Files.readString(Path.of("../specs/specifications.md"));

        assertTrue(new SpecificationIndex(text).ids().endsWith("SPEC-20"));
    }
}
