package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.AbstractPostgresIntegrationTest;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.ReviewEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

// Runs against real Postgres (AbstractPostgresIntegrationTest), not H2: this exercises the raw
// native UPDATE/subquery in TourJpaRepository.recalculateRatingStats, which is exactly the piece
// a mocked-repository unit test cannot verify — only a real database can confirm the SQL is
// actually correct end to end (AC: "điểm trung bình của tour cập nhật đúng").
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class TourJpaRepositoryRecalculateRatingStatsIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private TourJpaRepository tourJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void recalculateRatingStats_computesAverageAndCount_fromActualReviewRows() {
        TourEntity tour = persistTour();
        UserEntity reviewerA = persistUser("reviewer-a");
        UserEntity reviewerB = persistUser("reviewer-b");
        persistReview(tour, reviewerA, 4);
        persistReview(tour, reviewerB, 2);

        tourJpaRepository.recalculateRatingStats(tour.getId());
        entityManager.clear();

        TourEntity reloaded = tourJpaRepository.findById(tour.getId()).orElseThrow();
        assertThat(reloaded.getRatingAvg()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(reloaded.getReviewCount()).isEqualTo(2);
    }

    @Test
    void recalculateRatingStats_addingAThirdReview_shiftsTheAverage_andIncrementsCount() {
        TourEntity tour = persistTour();
        UserEntity reviewerA = persistUser("reviewer-a");
        UserEntity reviewerB = persistUser("reviewer-b");
        UserEntity reviewerC = persistUser("reviewer-c");
        persistReview(tour, reviewerA, 5);
        persistReview(tour, reviewerB, 5);
        tourJpaRepository.recalculateRatingStats(tour.getId());
        entityManager.clear();

        persistReview(entityManager.find(TourEntity.class, tour.getId()), reviewerC, 1);
        tourJpaRepository.recalculateRatingStats(tour.getId());
        entityManager.clear();

        TourEntity reloaded = tourJpaRepository.findById(tour.getId()).orElseThrow();
        assertThat(reloaded.getRatingAvg()).isEqualByComparingTo(new BigDecimal("3.67"));
        assertThat(reloaded.getReviewCount()).isEqualTo(3);
    }

    @Test
    void recalculateRatingStats_noReviews_leavesZeroAvgAndZeroCount() {
        TourEntity tour = persistTour();

        tourJpaRepository.recalculateRatingStats(tour.getId());
        entityManager.clear();

        TourEntity reloaded = tourJpaRepository.findById(tour.getId()).orElseThrow();
        assertThat(reloaded.getRatingAvg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reloaded.getReviewCount()).isZero();
    }

    private TourEntity persistTour() {
        return entityManager.persistAndFlush(TourEntity.builder()
                .title("IT Tour")
                .slug("it-tour-" + System.nanoTime())
                .price(BigDecimal.TEN)
                .ratingAvg(BigDecimal.ZERO)
                .reviewCount(0)
                .featured(false)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private UserEntity persistUser(String tag) {
        return entityManager.persistAndFlush(UserEntity.builder()
                .email(tag + "-" + System.nanoTime() + "@example.com")
                .passwordHash("hash")
                .role("USER")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private void persistReview(TourEntity tour, UserEntity user, int rating) {
        entityManager.persistAndFlush(ReviewEntity.builder()
                .tour(tour)
                .user(user)
                .rating(rating)
                .comment("comment")
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
