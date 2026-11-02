package dev.abakar.agenttest.claude;

import java.util.List;
import java.util.Map;

/** Point d'entrée unique vers le modèle : le reste du code ignore s'il est simulé ou réel. */
public interface ClaudeClient {

    String ask(String prompt);

    /** Envoie toute la conversation et la liste des outils ; la réponse peut demander des actions. */
    ClaudeReply converse(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools);

    String mode();
}
