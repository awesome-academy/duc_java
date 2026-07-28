package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.CreateReviewCommand;
import com.tripgoapi.application.port.in.CreateReviewUseCase;
import com.tripgoapi.application.port.in.GetTourReviewsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourReviewsResult;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.exception.ReviewNotAllowedException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.infrastructure.mapper.ReviewWebMapper;
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
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TourReviewControllerTest {

    private static final Long USER_ID = 99L;

    @Mock
    private GetTourReviewsUseCase getTourReviewsUseCase;
    @Mock
    private CreateReviewUseCase createReviewUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReviewWebMapper reviewWebMapper = Mappers.getMapper(ReviewWebMapper.class);
        TourReviewController controller = new TourReviewController(getTourReviewsUseCase, createReviewUseCase, reviewWebMapper);
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

    @Test
    void reviewsPageOutOfRange_isRejectedWith400_insteadOfSilentlyClamping() throws Exception {
        // Reviews endpoint previously clamped page/size with manual Math.max/min in the
        // controller; it now uses the same declarative validation as /tours for consistency.
        mockMvc.perform(get("/tours/1/reviews").param("size", "51"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getTourReviewsUseCase);
    }

    @Test
    void reviewsValidPageAndSize_arePassedThroughUnchanged() throws Exception {
        when(getTourReviewsUseCase.getReviews(anyLong(), anyInt(), anyInt()))
                .thenReturn(new TourReviewsResult(new PageResult<>(List.of(), 0, 1, 10), BigDecimal.ZERO));

        mockMvc.perform(get("/tours/1/reviews"))
                .andExpect(status().isOk());

        verify(getTourReviewsUseCase).getReviews(1L, 1, 10);
    }

    @Test
    void createReview_success_returns201_andUsesPrincipalUserId() throws Exception {
        Review saved = new Review(10L, null, 4, "Tuyệt vời", OffsetDateTime.parse("2026-07-20T10:00:00Z"));
        when(createReviewUseCase.createReview(any())).thenReturn(saved);

        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 4, \"comment\": \"Tuyệt vời\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.comment").value("Tuyệt vời"));

        ArgumentCaptor<CreateReviewCommand> captor = ArgumentCaptor.forClass(CreateReviewCommand.class);
        verify(createReviewUseCase).createReview(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().tourId()).isEqualTo(1L);
        assertThat(captor.getValue().rating()).isEqualTo(4);
        assertThat(captor.getValue().comment()).isEqualTo("Tuyệt vời");
    }

    @Test
    void createReview_ratingOutOfRange_returns422_andNeverCallsUseCase() throws Exception {
        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 6}"))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(createReviewUseCase);
    }

    @Test
    void createReview_missingRating_returns422_andNeverCallsUseCase() throws Exception {
        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(createReviewUseCase);
    }

    @Test
    void createReview_nonPositiveTourId_stillGoesToUseCase_andSurfacesAsTourNotFound() throws Exception {
        // tourId has no @Positive here (see TourReviewController.createReview): mixing it with
        // @Valid on the body would demote rating validation errors from 422 to 400. A
        // non-positive id simply matches no tour, so it 404s via the service instead of failing
        // fast at 400.
        when(createReviewUseCase.createReview(any())).thenThrow(new TourNotFoundException(0L));

        mockMvc.perform(post("/tours/0/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 4}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_tourNotFound_returns404() throws Exception {
        when(createReviewUseCase.createReview(any())).thenThrow(new TourNotFoundException(1L));

        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 4}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_noReviewableBooking_returns403() throws Exception {
        when(createReviewUseCase.createReview(any())).thenThrow(new ReviewNotAllowedException(1L));

        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 4}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReview_alreadyReviewed_returns409() throws Exception {
        when(createReviewUseCase.createReview(any())).thenThrow(new ReviewAlreadyExistsException(1L));

        mockMvc.perform(post("/tours/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 4}"))
                .andExpect(status().isConflict());
    }
}
