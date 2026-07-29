package com.tripgoapi.infrastructure.config;

import com.tripgoapi.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks in the exact security-matcher intent that used to be un-tested: GET under /tours/** stays
 * public (catalog browsing), while POST /tours/{id}/reviews requires authentication. Every other
 * test in this codebase for the review endpoint uses {@code standaloneSetup}, which never runs the
 * real filter chain — so a regression like re-adding "/tours/**" to PUBLIC_ENDPOINTS in
 * SecurityConfig (trivial to do when wiring up a new public tour endpoint) would go undetected:
 * the write endpoint would become public, {@code @AuthenticationPrincipal} would resolve to null,
 * and {@code principal.userId()} in TourReviewController would NPE into a 500 instead of a 401.
 */
@SpringBootTest
@ActiveProfiles("dev")
class SecurityConfigIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    /**
     * Built by hand rather than via {@code @AutoConfigureMockMvc}: this Boot version ships the
     * bridge that auto-applies {@code springSecurity()} in a module this project does not depend
     * on, so the real filter chain would silently be skipped without this.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void gettingATour_neverRequiresAuthentication() throws Exception {
        // Whether tour id=1 actually exists doesn't matter here — 401 is the one status this
        // request must never produce, since /tours/** GET is meant to be public catalog browsing.
        int status = mockMvc.perform(get("/tours/1")).andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(401);
    }

    @Test
    void creatingAReviewWithoutAToken_isRejectedAsUnauthorized_notServerError() throws Exception {
        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Tuyệt vời\"}"))
                .andExpect(status().isUnauthorized());
    }
}
