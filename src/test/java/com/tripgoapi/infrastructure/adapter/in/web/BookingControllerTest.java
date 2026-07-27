package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.CreateBookingCommand;
import com.tripgoapi.application.port.in.CreateBookingUseCase;
import com.tripgoapi.application.port.in.GetBookingsUseCase;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.exception.BookingGroupTooLargeException;
import com.tripgoapi.domain.exception.NoAvailableSlotsException;
import com.tripgoapi.domain.exception.TourDepartureNotFoundException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.infrastructure.mapper.BookingWebMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private static final Long USER_ID = 99L;

    @Mock
    private CreateBookingUseCase createBookingUseCase;
    @Mock
    private GetBookingsUseCase getBookingsUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BookingWebMapper bookingWebMapper = Mappers.getMapper(BookingWebMapper.class);
        BookingController controller = new BookingController(createBookingUseCase, getBookingsUseCase, bookingWebMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // Mirrors exactly what JwtAuthenticationFilter does in production — seeds the
        // SecurityContext directly since this standalone MockMvc setup runs no security filters.
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(USER_ID, "jane@example.com", Role.USER);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String validRequestBody() throws Exception {
        return """
                {
                  "idempotencyKey": "idem-key-1",
                  "tourId": 1,
                  "date": "2026-08-15",
                  "adults": 2,
                  "children": 1,
                  "contact": {
                    "name": "Jane",
                    "email": "jane@example.com",
                    "phone": "0900000000"
                  }
                }
                """;
    }

    private Booking sampleBooking() {
        return new Booking(
                1L, "TG-2026-000001", "idem-key-1", USER_ID, 1L, 3L, LocalDate.of(2026, 8, 15),
                2, 1, BigDecimal.valueOf(300), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", OffsetDateTime.now()
        );
    }

    @Test
    void createBooking_missingRequiredFields_returns422_andNeverCallsUseCase() throws Exception {
        // Every primitive field present (Jackson would reject a missing int with a raw
        // deserialization error, i.e. malformed 400, not the 422 this test is targeting) —
        // only Bean Validation rules are violated here: adults, contact.name/email/phone.
        String invalidBody = """
                {
                  "idempotencyKey": "idem-key-1",
                  "tourId": 1,
                  "date": "2026-08-15",
                  "adults": 0,
                  "children": 0,
                  "contact": { "name": "", "email": "not-an-email", "phone": "x" }
                }
                """;

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(createBookingUseCase);
    }

    @Test
    void createBooking_success_returns201_withBookingBody_andUsesPrincipalUserId_notAnyClientValue() throws Exception {
        when(createBookingUseCase.createBooking(any(CreateBookingCommand.class))).thenReturn(sampleBooking());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookingCode").value("TG-2026-000001"))
                .andExpect(jsonPath("$.data.totalPrice").value(300));

        ArgumentCaptor<CreateBookingCommand> captor = ArgumentCaptor.forClass(CreateBookingCommand.class);
        verify(createBookingUseCase).createBooking(captor.capture());
        CreateBookingCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(USER_ID);
        assertThat(command.idempotencyKey()).isEqualTo("idem-key-1");
        assertThat(command.tourId()).isEqualTo(1L);
        assertThat(command.adults()).isEqualTo(2);
        assertThat(command.children()).isEqualTo(1);
        assertThat(command.contactEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void createBooking_tourNotFound_returns404() throws Exception {
        when(createBookingUseCase.createBooking(any(CreateBookingCommand.class)))
                .thenThrow(new TourNotFoundException(1L));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_departureNotFound_returns404() throws Exception {
        when(createBookingUseCase.createBooking(any(CreateBookingCommand.class)))
                .thenThrow(new TourDepartureNotFoundException(1L, LocalDate.of(2026, 8, 15)));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_noAvailableSlots_returns409() throws Exception {
        when(createBookingUseCase.createBooking(any(CreateBookingCommand.class)))
                .thenThrow(new NoAvailableSlotsException());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isConflict());
    }

    @Test
    void createBooking_groupTooLarge_returns409() throws Exception {
        when(createBookingUseCase.createBooking(any(CreateBookingCommand.class)))
                .thenThrow(new BookingGroupTooLargeException(3, 2));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isConflict());
    }

    @Test
    void getBookings_returnsListForCurrentPrincipal() throws Exception {
        when(getBookingsUseCase.getBookingsForUser(USER_ID)).thenReturn(List.of(sampleBooking()));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bookingCode").value("TG-2026-000001"));

        verify(getBookingsUseCase).getBookingsForUser(USER_ID);
    }
}
