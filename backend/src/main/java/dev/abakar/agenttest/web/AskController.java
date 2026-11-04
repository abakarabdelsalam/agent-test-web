package dev.abakar.agenttest.web;

import dev.abakar.agenttest.claude.ClaudeClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class AskController {

    public record AskRequest(String prompt) {}

    public record AskResponse(String answer, String mode) {}

    private final ClaudeClient claude;

    public AskController(ClaudeClient claude) {
        this.claude = claude;
    }

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        return new AskResponse(claude.ask(request.prompt()), claude.mode());
    }
}
