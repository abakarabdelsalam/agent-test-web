package dev.abakar.agenttest.browser;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import dev.abakar.agenttest.report.BugReport;
import dev.abakar.agenttest.report.RunReport;
import dev.abakar.agenttest.report.Severity;
import org.springframework.stereotype.Component;

/** Vérifications écrites à la main au jour 2. Le modèle choisira lui-même ses actions au jour 3. */
@Component
public class CatalogExplorer {

    private static final int RENDER_DELAY_MS = 200;

    public RunReport explore(String url) {
        List<BugReport> bugs = new ArrayList<>();
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();
            checkSearch(page, url, bugs);
            checkSort(page, url, bugs);
            checkCategory(page, url, bugs);
        }
        return new RunReport(url, bugs);
    }

    private void open(Page page, String url) {
        page.navigate(url);
        page.getByTestId("product-card").first().waitFor();
    }

    private void checkSearch(Page page, String url, List<BugReport> bugs) {
        open(page, url);
        page.getByTestId("search").fill("casque");
        page.waitForTimeout(RENDER_DELAY_MS);
        if (page.getByTestId("product-card").count() == 0) {
            bugs.add(new BugReport("SPEC-01", "La recherche tient compte des majuscules",
                    "Le Casque Bluetooth apparaît", "Aucun produit trouvé", Severity.MAJEURE,
                    List.of("Ouvrir le catalogue", "Chercher « casque »")));
        }
    }

    private void checkSort(Page page, String url, List<BugReport> bugs) {
        open(page, url);
        page.getByTestId("sort").selectOption("asc");
        page.waitForTimeout(RENDER_DELAY_MS);
        List<Double> prices = page.getByTestId("card-price").allTextContents().stream()
                .map(CatalogExplorer::parsePrice)
                .toList();
        if (!prices.equals(prices.stream().sorted().toList())) {
            bugs.add(new BugReport("SPEC-02", "Le tri par prix croissant est faux",
                    "Le produit le moins cher en premier", "Premier prix affiché : " + prices.getFirst() + " €",
                    Severity.MAJEURE, List.of("Ouvrir le catalogue", "Choisir « Prix croissant »")));
        }
    }

    private void checkCategory(Page page, String url, List<BugReport> bugs) {
        open(page, url);
        page.getByTestId("category").selectOption("Audio");
        page.waitForTimeout(RENDER_DELAY_MS);
        List<String> names = page.locator("[data-testid='product-card'] h2").allTextContents();
        if (names.stream().anyMatch(name -> name.contains("Câble"))) {
            bugs.add(new BugReport("SPEC-03", "Le filtre Audio affiche un accessoire",
                    "Seuls les produits de la catégorie Audio", "Le Câble Audio Jack apparaît",
                    Severity.MINEURE, List.of("Ouvrir le catalogue", "Choisir la catégorie Audio")));
        }
    }

    static double parsePrice(String text) {
        return Double.parseDouble(text.replace("€", "").replace(",", ".").strip());
    }
}
