package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourReviewsResult;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTourReviewsServiceTest {

    @Mock
    private TourDetailRepositoryInterface tourDetailRepository;

    @Mock
    private ReviewRepositoryInterface reviewRepository;

    private GetTourReviewsService service;

    private GetTourReviewsService newService() {
        return new GetTourReviewsService(tourDetailRepository, reviewRepository);
    }

    @Test
    void throwsTourNotFound_whenTourDoesNotExist_withoutTouchingRatingOrReviews() {
        service = newService();
        Long tourId = 99L;
        when(tourDetailRepository.existsActiveTour(tourId)).thenReturn(false);

        assertThatThrownBy(() -> service.getReviews(tourId, 1, 10))
                .isInstanceOf(TourNotFoundException.class);

        verify(tourDetailRepository, never()).findRatingAvg(any());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void returnsZeroAverageRating_whenTourExistsButHasNoRatingYet() {
        // Regression test: tour exists but has never been reviewed, so ratingAvg is NULL in DB.
        // The adapter's findRatingAvg surfaces that as Optional.empty(); this must NOT be
        // mistaken for "tour not found" (that used to trigger a false 404).
        service = newService();
        Long tourId = 1L;
        when(tourDetailRepository.existsActiveTour(tourId)).thenReturn(true);
        when(tourDetailRepository.findRatingAvg(tourId)).thenReturn(Optional.empty());
        PageResult<Review> pageResult = new PageResult<>(List.of(), 0, 1, 10);
        when(reviewRepository.findReviews(tourId, 1, 10)).thenReturn(pageResult);

        TourReviewsResult result = service.getReviews(tourId, 1, 10);

        assertThat(result.averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.reviews()).isSameAs(pageResult);
    }

    @Test
    void returnsActualAverageRating_whenTourHasReviews() {
        service = newService();
        Long tourId = 2L;
        BigDecimal average = new BigDecimal("4.50");
        when(tourDetailRepository.existsActiveTour(tourId)).thenReturn(true);
        when(tourDetailRepository.findRatingAvg(tourId)).thenReturn(Optional.of(average));
        PageResult<Review> pageResult = new PageResult<>(List.of(), 3, 1, 10);
        when(reviewRepository.findReviews(tourId, 1, 10)).thenReturn(pageResult);

        TourReviewsResult result = service.getReviews(tourId, 1, 10);

        assertThat(result.averageRating()).isEqualByComparingTo(average);
    }

    @Test
    void passesPageAndSizeThroughToReviewRepository() {
        service = newService();
        Long tourId = 3L;
        when(tourDetailRepository.existsActiveTour(tourId)).thenReturn(true);
        when(tourDetailRepository.findRatingAvg(tourId)).thenReturn(Optional.of(BigDecimal.ONE));
        when(reviewRepository.findReviews(eq(tourId), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 2, 5));

        service.getReviews(tourId, 2, 5);

        verify(reviewRepository).findReviews(tourId, 2, 5);
    }
}
