package dev.abakar.agenttest.web;

import java.util.List;

import dev.abakar.agenttest.history.RunDetail;
import dev.abakar.agenttest.history.RunDetail.BugView;
import dev.abakar.agenttest.history.RunHistory;
import dev.abakar.agenttest.history.RunSummary;
import dev.abakar.agenttest.history.ValidationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class RunsController {

    public record ValidationRequest(ValidationStatus statut) {}

    private final RunHistory history;

    public RunsController(RunHistory history) {
        this.history = history;
    }

    @GetMapping("/runs")
    public List<RunSummary> runs() {
        return history.recent();
    }

    @GetMapping("/runs/{id}")
    public RunDetail run(@PathVariable long id) {
        return history.find(id).orElseThrow(() -> notFound("Parcours introuvable"));
    }

    @PostMapping("/bugs/{id}/validation")
    public BugView validate(@PathVariable long id, @RequestBody ValidationRequest request) {
        if (request.statut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ statut est obligatoire");
        }
        return history.validate(id, request.statut()).orElseThrow(() -> notFound("Bug introuvable"));
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
