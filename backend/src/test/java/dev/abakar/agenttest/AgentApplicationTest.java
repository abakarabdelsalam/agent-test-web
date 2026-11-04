package dev.abakar.agenttest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.abakar.agenttest.agent.Specifications;
import dev.abakar.agenttest.claude.ClaudeClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Démarre l'application complète, avec un vrai serveur web, comme mvn spring-boot:run.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgentApplicationTest {

    @Autowired
    private ClaudeClient claude;

    @Autowired
    private Specifications specifications;

    @Test
    void startsInFakeModeWithProjectSpecifications() {
        assertEquals("fake", claude.mode());
        assertTrue(specifications.text().contains("SPEC-20"));
    }
}
