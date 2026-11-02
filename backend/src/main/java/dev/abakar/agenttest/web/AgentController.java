package dev.abakar.agenttest.web;

import java.util.function.Supplier;

import dev.abakar.agenttest.agent.AgentRun;
import dev.abakar.agenttest.agent.ToolLoop;
import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.browser.BrowserSession;
import dev.abakar.agenttest.browser.PlaywrightBrowserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AgentController {

    public record AgentRequest(String url, String instruction) {}

    private final AllowedSites allowedSites;
    private final ToolLoop loop;
    private final Supplier<BrowserSession> browsers;

    @Autowired
    public AgentController(AllowedSites allowedSites, ToolLoop loop, @Value("${agent.headed}") boolean headed) {
        this(allowedSites, loop, () -> new PlaywrightBrowserSession(allowedSites, headed));
    }

    // Les tests passent un faux navigateur pour ne pas lancer Chromium.
    AgentController(AllowedSites allowedSites, ToolLoop loop, Supplier<BrowserSession> browsers) {
        this.allowedSites = allowedSites;
        this.loop = loop;
        this.browsers = browsers;
    }

    @PostMapping("/agent")
    public AgentRun run(@RequestBody AgentRequest request) {
        if (isBlank(request.url()) || isBlank(request.instruction())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les champs url et instruction sont obligatoires");
        }
        if (!allowedSites.allows(request.url())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Site non autorisé");
        }
        try (BrowserSession session = browsers.get()) {
            return loop.run(session, request.url(), request.instruction());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
