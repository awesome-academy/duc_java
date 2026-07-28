package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateReviewCommand;
import com.tripgoapi.application.port.in.CreateReviewUseCase;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.exception.ReviewNotAllowedException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateReviewService implements CreateReviewUseCase {

    private static final List<BookingStatus> REVIEWABLE_STATUSES = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final BookingRepositoryInterface bookingRepository;
    private final ReviewRepositoryInterface reviewRepository;

    @Override
    @Transactional
    public Review createReview(CreateReviewCommand command) {
        Long tourId = command.tourId();
        Long userId = command.userId();

        if (!tourDetailRepository.existsActiveTour(tourId)) {
            throw new TourNotFoundException(tourId);
        }

        if (!bookingRepository.existsByUserIdAndTourIdAndStatusIn(userId, tourId, REVIEWABLE_STATUSES)) {
            throw new ReviewNotAllowedException(tourId);
        }

        if (reviewRepository.existsByTourIdAndUserId(tourId, userId)) {
            throw new ReviewAlreadyExistsException(tourId);
        }

        Review review = reviewRepository.save(tourId, userId, command.rating(), command.comment());
        tourDetailRepository.recalculateRatingStats(tourId);
        return review;
    }
}
