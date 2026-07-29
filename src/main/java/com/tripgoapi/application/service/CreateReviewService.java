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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateReviewService implements CreateReviewUseCase {

    // CONFIRMED qualifies too, but only once its departure date has passed (see the
    // existsReviewEligibleBooking call below) — a booking for a trip that hasn't happened yet
    // must not let its owner post (and permanently lock in, since reviews aren't editable) a
    // review of an experience they haven't had.
    private static final List<BookingStatus> REVIEWABLE_STATUSES = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final BookingRepositoryInterface bookingRepository;
    private final ReviewRepositoryInterface reviewRepository;

    @Override
    @Transactional
    public Review createReview(CreateReviewCommand command) {
        Long tourId = command.tourId();
        Long userId = command.userId();

        // Locks the tour row before doing anything else. recalculateRatingStats recomputes from
        // the reviews table with a fresh per-statement snapshot, but under READ COMMITTED two
        // concurrent reviews for the same tour can still race: a blocked UPDATE resumes via
        // EvalPlanQual against the snapshot it originally took, so it can still miss a review the
        // other transaction just committed — see TourJpaRepository#recalculateRatingStats for the
        // full interleaving. Locking here serializes the two transactions, so the second one's
        // recalculate always runs against a snapshot taken after the first has committed.
        if (!tourDetailRepository.lockActiveTourForReview(tourId)) {
            throw new TourNotFoundException(tourId);
        }

        if (!bookingRepository.existsReviewEligibleBooking(userId, tourId, REVIEWABLE_STATUSES, LocalDate.now())) {
            throw new ReviewNotAllowedException(tourId);
        }

        if (reviewRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new ReviewAlreadyExistsException(tourId);
        }

        Review review = reviewRepository.save(userId, tourId, command.rating(), command.comment());
        tourDetailRepository.recalculateRatingStats(tourId);
        return review;
    }
}
