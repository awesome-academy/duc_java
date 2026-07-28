package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.ReviewEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.ReviewJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewPersistenceAdapterTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    private ReviewPersistenceAdapter newAdapter() {
        return new ReviewPersistenceAdapter(reviewJpaRepository);
    }

    @Test
    void findReviews_mapsEntities_usingUserFullNameAsReviewerName() {
        ReviewPersistenceAdapter adapter = newAdapter();
        ReviewEntity entity = ReviewEntity.builder()
                .id(1L)
                .tour(TourEntity.builder().id(2L).build())
                .user(UserEntity.builder().id(5L).fullName("Jane").build())
                .rating(4)
                .comment("Great tour")
                .createdAt(OffsetDateTime.now())
                .build();
        Page<ReviewEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(reviewJpaRepository.findByTour_Id(any(), any(Pageable.class))).thenReturn(page);

        PageResult<Review> result = adapter.findReviews(2L, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).reviewerName()).isEqualTo("Jane");
        assertThat(result.data().get(0).rating()).isEqualTo(4);
    }

    @Test
    void existsByTourIdAndUserId_delegatesToRepository() {
        ReviewPersistenceAdapter adapter = newAdapter();
        when(reviewJpaRepository.existsByTour_IdAndUser_Id(2L, 5L)).thenReturn(true);

        assertThat(adapter.existsByTourIdAndUserId(2L, 5L)).isTrue();
    }

    @Test
    void save_success_returnsDomainReviewWithGeneratedId() {
        ReviewPersistenceAdapter adapter = newAdapter();
        ArgumentCaptor<ReviewEntity> captor = ArgumentCaptor.forClass(ReviewEntity.class);
        when(reviewJpaRepository.saveAndFlush(captor.capture())).thenAnswer(invocation -> {
            ReviewEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        Review result = adapter.save(2L, 5L, 4, "Great tour");

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.rating()).isEqualTo(4);
        assertThat(result.comment()).isEqualTo("Great tour");
        assertThat(captor.getValue().getTour().getId()).isEqualTo(2L);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(5L);
    }

    @Test
    void save_raceLostToUniqueConstraint_translatesToReviewAlreadyExists() {
        // Regression test mirroring UserPersistenceAdapter: a concurrent duplicate review for
        // the same (tour, user) can slip past a existsByTourIdAndUserId pre-check and only get
        // caught here by the UNIQUE(tour_id, user_id) constraint on flush.
        ReviewPersistenceAdapter adapter = newAdapter();
        when(reviewJpaRepository.saveAndFlush(any(ReviewEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> adapter.save(2L, 5L, 4, "Great tour"))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }
}
