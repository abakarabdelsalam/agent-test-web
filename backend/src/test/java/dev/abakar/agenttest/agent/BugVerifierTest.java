package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BugVerifierTest {

    // Formes réellement possibles d'une confirmation : markdown, casse, accent décomposé.
    @ParameterizedTest
    @ValueSource(strings = {
            "CONFIRMÉ : le bouton reste actif.",
            "**CONFIRMÉ** : le bouton reste actif.",
            "Confirmé. Le bouton reste actif.",
            "CONFIRMÉ : accent décomposé.",
            "  # CONFIRMÉ",
            "CONFIRME sans accent."
    })
    void acceptsEveryFormOfConfirmation(String reply) {
        assertTrue(BugVerifier.isConfirmed(reply));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "REJETÉ : le comportement est voulu.",
            "**REJETÉ**",
            "NON CONFIRMÉ : aucune preuve.",
            "Je ne peux pas confirmer.",
            ""
    })
    void rejectsEverythingElse(String reply) {
        assertFalse(BugVerifier.isConfirmed(reply));
    }
}
