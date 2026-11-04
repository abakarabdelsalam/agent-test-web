package dev.abakar.agenttest.browser;

/** Un onglet de navigateur piloté par l'agent. Une session par parcours, fermée à la fin. */
public interface BrowserSession extends AutoCloseable {

    void open(String url);

    /** Texte visible et éléments utilisables de la page, lisibles par le modèle. */
    String readPage();

    void click(String selector);

    void fill(String selector, String text);

    /** Choisit une option d'une liste déroulante, par sa valeur ou son libellé. */
    void select(String selector, String option);

    /** Capture d'écran au format PNG. */
    byte[] capture();

    String currentUrl();

    @Override
    void close();
}
