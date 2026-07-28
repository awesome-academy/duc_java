package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateReviewCommand;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.exception.ReviewNotAllowedException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReviewServiceTest {

    private static final Long USER_ID = 5L;
    private static final Long TOUR_ID = 2L;

    @Mock
    private TourDetailRepositoryInterface tourDetailRepository;
    @Mock
    private BookingRepositoryInterface bookingRepository;
    @Mock
    private ReviewRepositoryInterface reviewRepository;

    private CreateReviewService newService() {
        return new CreateReviewService(tourDetailRepository, bookingRepository, reviewRepository);
    }

    private CreateReviewCommand newCommand() {
        return new CreateReviewCommand(USER_ID, TOUR_ID, 4, "Tuyệt vời");
    }

    @Test
    void tourNotFound_throwsTourNotFoundException_neverTouchesBookingOrReview() {
        CreateReviewService service = newService();
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createReview(newCommand()))
                .isInstanceOf(TourNotFoundException.class);

        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void noConfirmedOrCompletedBooking_throwsReviewNotAllowedException_neverSaves() {
        CreateReviewService service = newService();
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(true);
        when(bookingRepository.existsByUserIdAndTourIdAndStatusIn(USER_ID, TOUR_ID,
                List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED))).thenReturn(false);

        assertThatThrownBy(() -> service.createReview(newCommand()))
                .isInstanceOf(ReviewNotAllowedException.class);

        verifyNoInteractions(reviewRepository);
        verify(tourDetailRepository, never()).recalculateRatingStats(any());
    }

    @Test
    void alreadyReviewed_throwsReviewAlreadyExistsException_neverSavesOrRecalculates() {
        CreateReviewService service = newService();
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(true);
        when(bookingRepository.existsByUserIdAndTourIdAndStatusIn(anyLong(), anyLong(), any())).thenReturn(true);
        when(reviewRepository.existsByTourIdAndUserId(TOUR_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(newCommand()))
                .isInstanceOf(ReviewAlreadyExistsException.class);

        verify(reviewRepository, never()).save(anyLong(), anyLong(), anyInt(), anyString());
        verify(tourDetailRepository, never()).recalculateRatingStats(any());
    }

    @Test
    void validRequest_savesReview_thenRecalculatesTourRatingStats() {
        CreateReviewService service = newService();
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(true);
        when(bookingRepository.existsByUserIdAndTourIdAndStatusIn(anyLong(), anyLong(), any())).thenReturn(true);
        when(reviewRepository.existsByTourIdAndUserId(TOUR_ID, USER_ID)).thenReturn(false);
        Review saved = new Review(10L, null, 4, "Tuyệt vời", OffsetDateTime.now());
        when(reviewRepository.save(TOUR_ID, USER_ID, 4, "Tuyệt vời")).thenReturn(saved);

        Review result = service.createReview(newCommand());

        assertThat(result).isSameAs(saved);
        verify(reviewRepository).save(TOUR_ID, USER_ID, 4, "Tuyệt vời");
        verify(tourDetailRepository).recalculateRatingStats(TOUR_ID);
    }
}
