package dev.abakar.agenttest.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import dev.abakar.agenttest.browser.AllowedSites;
import dev.abakar.agenttest.browser.CatalogExplorer;
import dev.abakar.agenttest.report.ReportValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ExploreControllerTest {

    private final CatalogExplorer explorer = mock(CatalogExplorer.class);

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ExploreController(
            new AllowedSites(Set.of("localhost")),
            explorer,
            new ReportValidator(Validation.buildDefaultValidatorFactory().getValidator())))
            .build();

    @Test
    void refusesRequestWithoutBody() throws Exception {
        mvc.perform(post("/api/explore").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(explorer);
    }

    @Test
    void refusesJsonNull() throws Exception {
        mvc.perform(post("/api/explore").contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(explorer);
    }

    @Test
    void refusesRequestWithoutUrl() throws Exception {
        mvc.perform(post("/api/explore").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(explorer);
    }

    @Test
    void refusesBlankUrl() throws Exception {
        mvc.perform(post("/api/explore").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(explorer);
    }

    @Test
    void refusesSiteNotAllowed() throws Exception {
        mvc.perform(post("/api/explore").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(explorer);
    }
}
