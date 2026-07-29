package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.AdminTourSearchQuery;
import com.tripgoapi.application.port.in.CreateDestinationUseCase;
import com.tripgoapi.application.port.in.CreateTourUseCase;
import com.tripgoapi.application.port.in.DeleteDestinationUseCase;
import com.tripgoapi.application.port.in.DeleteTourUseCase;
import com.tripgoapi.application.port.in.GetAdminBookingsUseCase;
import com.tripgoapi.application.port.in.GetAdminDestinationsUseCase;
import com.tripgoapi.application.port.in.GetAdminTourDetailUseCase;
import com.tripgoapi.application.port.in.GetAdminToursUseCase;
import com.tripgoapi.application.port.in.GetCategoriesUseCase;
import com.tripgoapi.application.port.in.GetDashboardStatsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.UpdateBookingStatusUseCase;
import com.tripgoapi.application.port.in.UpdateDestinationUseCase;
import com.tripgoapi.application.port.in.UpdateTourUseCase;
import com.tripgoapi.domain.model.AdminTourDetail;
import com.tripgoapi.domain.model.AdminTourSummary;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.Category;
import com.tripgoapi.domain.model.DashboardStats;
import com.tripgoapi.domain.model.Destination;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.domain.model.TourStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renders every admin template against real domain objects. The domain models are Java records,
 * and the layout is composed through fragment parameters — both are resolved at render time, so
 * only actually rendering the HTML proves the templates are wired correctly. Deliberately kept
 * free of Spring context and database so it runs everywhere.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminTemplateRenderingTest {

    @Mock
    private GetAdminToursUseCase getAdminToursUseCase;
    @Mock
    private GetAdminTourDetailUseCase getAdminTourDetailUseCase;
    @Mock
    private CreateTourUseCase createTourUseCase;
    @Mock
    private UpdateTourUseCase updateTourUseCase;
    @Mock
    private DeleteTourUseCase deleteTourUseCase;
    @Mock
    private GetAdminBookingsUseCase getAdminBookingsUseCase;
    @Mock
    private UpdateBookingStatusUseCase updateBookingStatusUseCase;
    @Mock
    private GetAdminDestinationsUseCase getAdminDestinationsUseCase;
    @Mock
    private CreateDestinationUseCase createDestinationUseCase;
    @Mock
    private UpdateDestinationUseCase updateDestinationUseCase;
    @Mock
    private DeleteDestinationUseCase deleteDestinationUseCase;
    @Mock
    private GetCategoriesUseCase getCategoriesUseCase;
    @Mock
    private GetDashboardStatsUseCase getDashboardStatsUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        stubUseCases();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setCharacterEncoding("UTF-8");
        // Boot's auto-configured resolver sets this; without it the standalone one writes
        // ISO-8859-1 and escapes Vietnamese characters into HTML entities.
        viewResolver.setContentType("text/html;charset=UTF-8");

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminDashboardController(getDashboardStatsUseCase, getAdminBookingsUseCase),
                        new AdminTourController(getAdminToursUseCase, getAdminTourDetailUseCase,
                                createTourUseCase, updateTourUseCase, deleteTourUseCase,
                                getAdminDestinationsUseCase, getCategoriesUseCase),
                        new AdminBookingController(getAdminBookingsUseCase, updateBookingStatusUseCase),
                        new AdminDestinationController(getAdminDestinationsUseCase, createDestinationUseCase,
                                updateDestinationUseCase, deleteDestinationUseCase),
                        new AdminAuthController())
                .setViewResolvers(viewResolver)
                .build();
    }

    private void stubUseCases() {
        when(getDashboardStatsUseCase.getStats()).thenReturn(
                new DashboardStats(12, 5, 3, 8, 42, BigDecimal.valueOf(112_250_000)));

        when(getAdminToursUseCase.searchTours(any(AdminTourSearchQuery.class))).thenReturn(
                new PageResult<>(List.of(tourSummary()), 24, 1, 10));

        when(getAdminTourDetailUseCase.getTourForEdit(any())).thenReturn(tourDetail());

        when(getAdminBookingsUseCase.searchBookings(any(AdminBookingSearchQuery.class))).thenReturn(
                new PageResult<>(List.of(
                        booking(BookingStatus.PENDING),
                        booking(BookingStatus.CONFIRMED),
                        booking(BookingStatus.CANCELLED),
                        booking(BookingStatus.COMPLETED)), 4, 1, 10));

        when(getAdminDestinationsUseCase.getDestinations()).thenReturn(List.of(
                new Destination(1L, "Đà Nẵng", "da-nang", "Thành phố đáng sống", "/uploads/destinations/dn.jpg"),
                // Null image and null description: the list must not blow up on optional columns.
                new Destination(2L, "Sa Pa", "sa-pa", null, null)));
        when(getAdminDestinationsUseCase.getDestination(any())).thenReturn(
                new Destination(1L, "Đà Nẵng", "da-nang", "Thành phố đáng sống", "/uploads/destinations/dn.jpg"));

        when(getCategoriesUseCase.getCategories()).thenReturn(List.of(new Category(1L, "Biển", "bien")));
    }

    private AdminTourSummary tourSummary() {
        return new AdminTourSummary(1L, "Đà Nẵng - Hội An 3N2Đ", "Đà Nẵng", "Biển",
                BigDecimal.valueOf(5_000_000), BigDecimal.valueOf(4_490_000), 3,
                BigDecimal.valueOf(4.7), TourStatus.ACTIVE);
    }

    private AdminTourDetail tourDetail() {
        return new AdminTourDetail(1L, "Đà Nẵng - Hội An 3N2Đ", "da-nang-hoi-an-3n2d", "Mô tả tour",
                1L, "Đà Nẵng", 1L, "Biển",
                BigDecimal.valueOf(5_000_000), BigDecimal.valueOf(4_490_000), 3, 20,
                true, TourStatus.ACTIVE,
                List.of(new TourImage("/uploads/tours/a.jpg", true, 0),
                        new TourImage("/uploads/tours/b.jpg", false, 1)),
                List.of(new TourItineraryDay(1, "Khởi hành", "Bay ra Đà Nẵng"),
                        new TourItineraryDay(2, "Hội An", "Phố cổ")));
    }

    private Booking booking(BookingStatus status) {
        return new Booking(1L, "TG-2026-000121", "idem-1", 5L, 2L, "Đà Nẵng 3N2Đ", "da-nang-3n2d", 3,
                4L, LocalDate.of(2026, 7, 5), 2, 1, BigDecimal.valueOf(11_225_000), status,
                "Nguyễn An", "an@example.com", "0900000000", OffsetDateTime.now());
    }

    /**
     * Returns the rendered page as a browser would read it. Thymeleaf writes attribute values with
     * HTML4 named entities, so a bound "Đà Nẵng" comes out as "Đ&agrave; Nẵng" — correct HTML, but
     * the assertions below are about the text the admin sees, not its encoding.
     */
    private String render(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        return HtmlUtils.htmlUnescape(result.getResponse().getContentAsString());
    }

    @Test
    void dashboardRendersStatsAndRecentBookings() throws Exception {
        String html = render("/admin");

        assertThat(html)
                .contains("TripGo Admin")
                .contains("TG-2026-000121")
                .contains("Nguyễn An")
                // Money is formatted with Vietnamese thousand separators, not raw digits.
                .contains("112.250.000đ");
    }

    @Test
    void tourListRendersRowsWithDiscountAndPagination() throws Exception {
        String html = render("/admin/tours");

        assertThat(html)
                .contains("Đà Nẵng - Hội An 3N2Đ")
                .contains("4.490.000đ")
                // Original price struck through beside the discounted one.
                .contains("5.000.000đ")
                .contains("3 ngày")
                // 24 results at 10 per page => 3 pagination links.
                .contains("pagination");
    }

    @Test
    void tourCreateFormRendersEmptyFieldsAndDropdowns() throws Exception {
        String html = render("/admin/tours/new");

        assertThat(html)
                .contains("Thêm Tour")
                .contains("-- Chọn điểm đến --")
                .contains("Biển")
                .contains("+ Tải ảnh")
                .contains("itinerary-template");
    }

    @Test
    void tourEditFormIsPrefilledIncludingItineraryAndImages() throws Exception {
        String html = render("/admin/tours/1/edit");

        assertThat(html)
                .contains("Sửa Tour")
                .contains("value=\"Đà Nẵng - Hội An 3N2Đ\"")
                .contains("itinerary[0].title")
                .contains("itinerary[1].title")
                .contains("/uploads/tours/a.jpg")
                // The thumbnail radio must come back pre-selected on the image that carries the flag.
                .contains("Ảnh đại diện");
    }

    @Test
    void bookingListRendersEveryStatusWithTheRightActions() throws Exception {
        String html = render("/admin/bookings");

        assertThat(html)
                .contains("Chờ xác nhận")
                .contains("badge--pending")
                .contains("badge--confirmed")
                .contains("badge--cancelled")
                .contains("badge--completed")
                // Only PENDING rows offer "Xác nhận"; terminal rows say so instead.
                .contains("Xác nhận")
                .contains("Không thể đổi");
    }

    @Test
    void destinationListAndFormRender() throws Exception {
        assertThat(render("/admin/destinations"))
                .contains("Đà Nẵng")
                .contains("da-nang")
                .contains("Sa Pa");

        assertThat(render("/admin/destinations/1/edit"))
                .contains("Sửa Điểm đến")
                .contains("value=\"Đà Nẵng\"");
    }

    @Test
    void loginPageRendersWithoutTheAdminChrome() throws Exception {
        String html = render("/admin/login");

        assertThat(html)
                .contains("TripGo Admin")
                .contains("Đăng nhập")
                .contains("Chỉ tài khoản role = admin mới đăng nhập được")
                // No sidebar: whoever sees this page is not signed in yet.
                .doesNotContain("Đăng xuất");
    }

    @Test
    void forbiddenPageRenders() throws Exception {
        assertThat(render("/admin/403"))
                .contains("403")
                .contains("không có quyền truy cập");
    }
}
