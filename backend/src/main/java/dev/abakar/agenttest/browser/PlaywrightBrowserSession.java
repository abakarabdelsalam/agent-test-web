package dev.abakar.agenttest.browser;

import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

/** Session Playwright réelle, utilisée par l'agent. */
public class PlaywrightBrowserSession implements BrowserSession {

    private static final int ACTION_TIMEOUT_MS = 5_000;
    private static final int RENDER_DELAY_MS = 200;
    private static final int MAX_TEXT_CHARS = 4_000;

    // Liste les éléments cliquables ou remplissables avec un sélecteur que le modèle peut réutiliser.
    private static final String ELEMENTS_SCRIPT = """
            () => {
              const seen = new Set();
              const lines = [];
              for (const e of document.querySelectorAll('[data-testid], a, button, input, select, textarea')) {
                const tag = e.tagName.toLowerCase();
                const id = e.getAttribute('data-testid');
                const label = (e.innerText || e.value || e.getAttribute('placeholder') || '')
                    .trim().replace(/\\s+/g, ' ').slice(0, 60);
                const selector = id ? `[data-testid="${id}"]` : `${tag}:has-text("${label.replaceAll('"', '\\\\"')}")`;
                const line = `${tag}${e.disabled ? ' (désactivé)' : ''} | ${selector} | ${label}`;
                if (!seen.has(line)) { seen.add(line); lines.push(line); }
                if (lines.length === 60) break;
              }
              return lines;
            }
            """;

    private final AllowedSites allowedSites;
    private final Playwright playwright;
    private final Browser browser;
    private final Page page;

    public PlaywrightBrowserSession(AllowedSites allowedSites) {
        this.allowedSites = allowedSites;
        playwright = Playwright.create();
        try {
            browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            // Deuxième garde-fou : même un clic sur un lien externe ne sort pas des hôtes autorisés.
            context.route("**/*", route -> {
                if (allowedSites.allows(route.request().url())) {
                    route.resume();
                } else {
                    route.abort();
                }
            });
            page = context.newPage();
            page.setDefaultTimeout(ACTION_TIMEOUT_MS);
        } catch (RuntimeException e) {
            playwright.close();
            throw e;
        }
    }

    @Override
    public void open(String url) {
        page.navigate(url);
        settle();
    }

    @Override
    public String readPage() {
        String text = page.locator("body").innerText();
        if (text.length() > MAX_TEXT_CHARS) {
            text = text.substring(0, MAX_TEXT_CHARS) + "\n[texte tronqué]";
        }
        List<?> elements = (List<?>) page.evaluate(ELEMENTS_SCRIPT);
        StringBuilder result = new StringBuilder()
                .append("URL : ").append(page.url()).append('\n')
                .append("Titre : ").append(page.title()).append("\n\n")
                .append("Texte visible :\n").append(text).append("\n\n")
                .append("Éléments utilisables (balise | sélecteur | libellé) :\n");
        elements.forEach(line -> result.append("- ").append(line).append('\n'));
        return result.toString();
    }

    @Override
    public void click(String selector) {
        page.locator(selector).first().click();
        settle();
        // La requête bloquée laisse Chromium sur sa page d'erreur : on revient sur le site testé.
        if (!allowedSites.allows(page.url())) {
            page.goBack();
            throw new IllegalStateException("Navigation bloquée : ce lien sort des sites autorisés.");
        }
    }

    @Override
    public void fill(String selector, String text) {
        page.locator(selector).first().fill(text);
        settle();
    }

    @Override
    public byte[] capture() {
        return page.screenshot();
    }

    @Override
    public String currentUrl() {
        return page.url();
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }

    // Angular met à jour la page juste après l'action : on laisse le rendu se terminer.
    private void settle() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(RENDER_DELAY_MS);
    }
}
