package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.AbstractPostgresIntegrationTest;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.WishlistJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

// Runs against real Postgres (AbstractPostgresIntegrationTest), not H2: the bug this guards
// against — REQUIRES_NEW + try/catch not actually saving a poisoned transaction on a duplicate
// insert — only reproduces with Postgres's real constraint-violation/rollback behavior.
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(WishlistPersistenceAdapter.class)
class WishlistPersistenceAdapterIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private WishlistPersistenceAdapter adapter;

    @Autowired
    private WishlistJpaRepository wishlistJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void add_twice_isIdempotent_andKeepsExactlyOneRow() {
        Long userId = persistUser().getId();
        Long tourId = persistTour().getId();

        adapter.add(userId, tourId);
        adapter.add(userId, tourId);

        assertThat(wishlistJpaRepository.count()).isEqualTo(1);
    }

    private UserEntity persistUser() {
        return entityManager.persistAndFlush(UserEntity.builder()
                .email("wishlist-it-" + System.nanoTime() + "@example.com")
                .passwordHash("hash")
                .role("USER")
                .createdAt(OffsetDateTime.now())
                .build());
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
}
