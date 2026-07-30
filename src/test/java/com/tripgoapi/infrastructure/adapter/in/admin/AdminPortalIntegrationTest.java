package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.HtmlUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * End-to-end cover for the acceptance criteria of the admin portal: who can get in, and whether a
 * change made through the UI is visible to the public API. The "dev" profile is what loads
 * {@code db/seed}, which is where the admin account comes from.
 */
@SpringBootTest
@ActiveProfiles("dev")
class AdminPortalIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@tripgo.vn";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String CUSTOMER_EMAIL = "nguyenvana@example.com";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    /**
     * Built by hand rather than via {@code @AutoConfigureMockMvc}: Boot 4 ships the bridge that
     * auto-applies {@code springSecurity()} in a separate module that this project does not depend
     * on, and without that configurer {@code @WithMockUser} never reaches the filter chain — every
     * "signed in" request would silently arrive anonymous and redirect to the login page.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void anonymousVisitorToTheAdminRoot_isRedirectedToTheLoginPage() throws Exception {
        // "/admin" has no trailing slash, so it only matches because the chain's securityMatcher
        // lists it explicitly alongside "/admin/**".
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void anonymousVisitorToAnAdminSubPage_isRedirectedToTheLoginPage() throws Exception {
        mockMvc.perform(get("/admin/tours"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void loginPageItself_isReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void adminCredentials_areAccepted_andLandOnTheDashboard() throws Exception {
        mockMvc.perform(formLogin("/admin/login")
                        .userParameter("email").user(ADMIN_EMAIL)
                        .passwordParam("password").password(ADMIN_PASSWORD))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void customerCredentials_areRejectedAtTheAdminLoginForm() throws Exception {
        // Rejected by AdminUserDetailsService before the password is even checked: a customer must
        // never obtain an admin session, correct password or not.
        mockMvc.perform(formLogin("/admin/login")
                        .userParameter("email").user(CUSTOMER_EMAIL)
                        .passwordParam("password").password("whatever"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void signedInCustomer_cannotReachTheAdminArea() throws Exception {
        mockMvc.perform(get("/admin/tours"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanRenderEveryListPage() throws Exception {
        // Also proves Thymeleaf can read the record-based domain models the pages iterate over —
        // a template expression failure would surface here as a 500.
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));

        mockMvc.perform(get("/admin/tours"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tours/list"))
                .andExpect(model().attributeExists("tours", "pageInfo"));

        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/bookings/list"));

        mockMvc.perform(get("/admin/destinations"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/destinations/list"));

        mockMvc.perform(get("/admin/tours/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tours/form"))
                .andExpect(model().attributeExists("destinations", "categories", "statuses"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidTourForm_isRedisplayedWithFieldErrors_insteadOfBeingSaved() throws Exception {
        mockMvc.perform(multipart("/admin/tours")
                        .param("title", "")
                        .param("price", "1000000")
                        // Discount >= price is a cross-field rule no single annotation can express.
                        .param("discountPrice", "2000000")
                        .param("durationDays", "0")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tours/form"))
                .andExpect(model().attributeHasFieldErrors(
                        "tourForm", "title", "destinationId", "discountPrice", "durationDays"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tourCreatedThroughTheUi_isImmediatelyVisibleOnThePublicApi() throws Exception {
        String title = "Tour kiểm thử quản trị " + System.nanoTime();

        mockMvc.perform(multipart("/admin/tours")
                        .param("title", title)
                        .param("destinationId", "1")
                        .param("price", "4990000")
                        .param("durationDays", "3")
                        .param("status", "ACTIVE")
                        .param("itinerary[0].title", "Khởi hành")
                        .param("itinerary[0].description", "Bay ra Đà Nẵng")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tours"));

        // The public /tours endpoint is anonymous and reads only ACTIVE tours — exactly the
        // "thay đổi phản ánh ở cả API công khai" criterion.
        MvcResult result = mockMvc.perform(get("/tours").param("q", title))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains(title);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletedTour_disappearsFromBothTheAdminListAndThePublicApi() throws Exception {
        String title = "Tour sẽ bị xóa " + System.nanoTime();

        mockMvc.perform(multipart("/admin/tours")
                        .param("title", title)
                        .param("destinationId", "1")
                        .param("price", "1990000")
                        .param("durationDays", "2")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Long tourId = extractTourId(title);

        mockMvc.perform(multipart("/admin/tours/{id}/delete", tourId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tours"));

        MvcResult adminList = mockMvc.perform(get("/admin/tours").param("q", title)).andReturn();
        // Unescape first (Thymeleaf writes "xóa" as "x&oacute;a"), then assert on the row cell
        // rather than the whole page: the search box echoes `q` back into the HTML, so a plain
        // doesNotContain(title) could never pass no matter what the table actually shows.
        String adminHtml = HtmlUtils.htmlUnescape(adminList.getResponse().getContentAsString());
        assertThat(adminHtml)
                .doesNotContain("<td class=\"cell-strong\">" + title + "</td>")
                .contains("Không có tour nào khớp điều kiện tìm kiếm.");

        MvcResult publicList = mockMvc.perform(get("/tours").param("q", title)).andReturn();
        assertThat(publicList.getResponse().getContentAsString()).doesNotContain(title);
    }

    /** Reads the new tour's id back off the public API rather than guessing a sequence value. */
    private Long extractTourId(String title) throws Exception {
        String body = mockMvc.perform(get("/tours").param("q", title))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertThat(matcher.find()).as("tour '%s' should be on the public API", title).isTrue();
        return Long.valueOf(matcher.group(1));
    }
}
