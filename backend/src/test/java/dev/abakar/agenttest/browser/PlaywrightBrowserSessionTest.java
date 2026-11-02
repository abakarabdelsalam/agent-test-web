package dev.abakar.agenttest.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Lance un vrai Chromium sur une petite page servie en local. */
class PlaywrightBrowserSessionTest {

    private static final String PAGE = """
            <!doctype html><html><head><title>Mini boutique</title></head><body>
            <h1>Catalogue</h1>
            <input data-testid="search" placeholder="Rechercher">
            <button data-testid="go" onclick="document.querySelector('h1').innerText =
                'Résultat : ' + document.querySelector('input').value">Chercher</button>
            <a href="https://example.com/">Lien externe</a>
            </body></html>
            """;

    private static HttpServer server;
    private static String url;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] body = PAGE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void readsFillsClicksAndCaptures() {
        try (BrowserSession session = new PlaywrightBrowserSession(new AllowedSites(Set.of("127.0.0.1")))) {
            session.open(url);
            String page = session.readPage();
            assertTrue(page.contains("Titre : Mini boutique"));
            assertTrue(page.contains("[data-testid=\"search\"]"));

            session.fill("[data-testid=\"search\"]", "casque");
            session.click("[data-testid=\"go\"]");
            assertTrue(session.readPage().contains("Résultat : casque"));

            byte[] png = session.capture();
            assertTrue(Arrays.equals(new byte[] {(byte) 0x89, 'P', 'N', 'G'}, Arrays.copyOf(png, 4)));
        }
    }

    @Test
    void observationModeShowsWindowAndPausesOneSecond() {
        assertFalse(PlaywrightBrowserSession.launchOptions(true).headless);
        assertEquals(1_000, PlaywrightBrowserSession.pauseFor(true));
    }

    @Test
    void defaultModeStaysHiddenWithoutPause() {
        assertTrue(PlaywrightBrowserSession.launchOptions(false).headless);
        assertEquals(0, PlaywrightBrowserSession.pauseFor(false));
    }

    @Test
    void staysOnAllowedHostAfterExternalLink() {
        try (BrowserSession session = new PlaywrightBrowserSession(new AllowedSites(Set.of("127.0.0.1")))) {
            session.open(url);
            assertThrows(IllegalStateException.class, () -> session.click("a:has-text(\"Lien externe\")"));
            assertEquals(url, session.currentUrl());
        }
    }
}
