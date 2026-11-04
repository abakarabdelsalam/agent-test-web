package dev.abakar.agenttest.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import dev.abakar.agenttest.agent.TestLoops;
import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.browser.BrowserSession;
import dev.abakar.agenttest.claude.FakeClaudeClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AgentControllerTest {

    private final AtomicInteger launched = new AtomicInteger();
    private final StubSession session = new StubSession();
    private final AllowedSites allowedSites = new AllowedSites(Set.of("localhost"));

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AgentController(allowedSites,
            TestLoops.loop(new FakeClaudeClient()),
            () -> {
                launched.incrementAndGet();
                return session;
            })).build();

    private void expectRefused(String body, int status) throws Exception {
        mvc.perform(post("/api/agent").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(status));
    }

    @Test
    void refusesMissingInstruction() throws Exception {
        expectRefused("{\"url\":\"http://localhost:4200\"}", 400);
    }

    @Test
    void refusesMissingUrl() throws Exception {
        expectRefused("{\"instruction\":\"Cherche casque\"}", 400);
    }

    @Test
    void refusesSiteNotAllowedWithoutLaunchingBrowser() throws Exception {
        expectRefused("{\"url\":\"https://example.com\",\"instruction\":\"Ouvre\"}", 403);
        assertTrue(launched.get() == 0);
    }

    @Test
    void runsScenarioAndClosesBrowser() throws Exception {
        mvc.perform(post("/api/agent").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://localhost:4200\",\"instruction\":\"Cherche casque\"}"))
                .andExpect(status().isOk())
                // PowerShell 5.1 lit les accents de travers si le charset n'est pas annoncé.
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.endedBy").value("end_turn"))
                .andExpect(jsonPath("$.actions.length()").value(4));
        assertTrue(session.closed);
    }

    private static class StubSession implements BrowserSession {
        boolean closed;
        private String url = "about:blank";

        @Override public void open(String url) { this.url = url; }
        @Override public String readPage() { return "Catalogue"; }
        @Override public void click(String selector) { }
        @Override public void fill(String selector, String text) { }
        @Override public void select(String selector, String option) { }
        @Override public byte[] capture() { return new byte[0]; }
        @Override public String currentUrl() { return url; }
        @Override public void close() { closed = true; }
    }
}
