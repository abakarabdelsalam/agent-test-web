package dev.abakar.agenttest.web;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.browser.CatalogExplorer;
import dev.abakar.agenttest.report.ReportValidator;
import dev.abakar.agenttest.report.RunReport;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class ExploreController {

    public record ExploreRequest(String url) {}

    private final AllowedSites allowedSites;
    private final CatalogExplorer explorer;
    private final ReportValidator validator;

    public ExploreController(AllowedSites allowedSites, CatalogExplorer explorer, ReportValidator validator) {
        this.allowedSites = allowedSites;
        this.explorer = explorer;
        this.validator = validator;
    }

    @PostMapping("/explore")
    public RunReport explore(@RequestBody ExploreRequest request) {
        // Une requête mal formée n'est pas un site interdit : 400 plutôt que 403.
        if (request.url() == null || request.url().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ url est obligatoire");
        }
        if (!allowedSites.allows(request.url())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Site non autorisé");
        }
        return validator.validate(explorer.explore(request.url()));
    }
}
