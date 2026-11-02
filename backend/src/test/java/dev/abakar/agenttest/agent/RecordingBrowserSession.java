package dev.abakar.agenttest.agent;

import java.util.ArrayList;
import java.util.List;

import dev.abakar.agenttest.browser.BrowserSession;

/** Faux navigateur : note chaque action au lieu de lancer Chromium. */
class RecordingBrowserSession implements BrowserSession {

    final List<String> calls = new ArrayList<>();
    String url = "about:blank";
    boolean closed;

    @Override
    public void open(String url) {
        calls.add("open " + url);
        this.url = url;
    }

    @Override
    public String readPage() {
        calls.add("read");
        return "Texte visible :\nCatalogue";
    }

    @Override
    public void click(String selector) {
        calls.add("click " + selector);
        if (selector.contains("introuvable")) {
            throw new IllegalStateException("Timeout 5000ms exceeded.\nCall log: ...");
        }
    }

    @Override
    public void fill(String selector, String text) {
        calls.add("fill " + selector + " " + text);
    }

    @Override
    public byte[] capture() {
        calls.add("capture");
        return new byte[2048];
    }

    @Override
    public String currentUrl() {
        return url;
    }

    @Override
    public void close() {
        closed = true;
    }
}
