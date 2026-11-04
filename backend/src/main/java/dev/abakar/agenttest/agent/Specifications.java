package dev.abakar.agenttest.agent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Le comportement attendu du site, donné à l'agent pour qu'il sache ce qu'est un bug. */
@Component
public class Specifications {

    private static final Logger LOG = LoggerFactory.getLogger(Specifications.class);

    private final String text;

    public Specifications(@Value("${agent.specs-path}") Path path) {
        this.text = read(path);
    }

    public String text() {
        return text;
    }

    // Sans spécifications, l'agent peut encore suivre une consigne : on prévient sans bloquer le démarrage.
    static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Spécifications introuvables : {}", path.toAbsolutePath());
            return "";
        }
    }
}
