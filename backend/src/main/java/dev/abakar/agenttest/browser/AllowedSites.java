package dev.abakar.agenttest.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Premier garde-fou : le navigateur n'ouvre que les hôtes autorisés. */
@Component
public class AllowedSites {

    private final Set<String> hosts;

    public AllowedSites(@Value("${agent.allowed-hosts}") Set<String> hosts) {
        this.hosts = Set.copyOf(hosts);
    }

    public boolean allows(String url) {
        if (url == null) {
            return false;
        }
        try {
            URI uri = new URI(url);
            boolean web = "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
            return web && uri.getHost() != null && hosts.contains(uri.getHost());
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
