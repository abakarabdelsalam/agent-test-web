package dev.abakar.agenttest.claude;

/** Point d'entrée unique vers le modèle : le reste du code ignore s'il est simulé ou réel. */
public interface ClaudeClient {

    String ask(String prompt);

    String mode();
}
