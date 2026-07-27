package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.DestinationEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.WishlistEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.WishlistJpaRepository;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistPersistenceAdapterTest {

    @Mock
    private WishlistJpaRepository wishlistJpaRepository;

    private WishlistPersistenceAdapter newAdapter() {
        return new WishlistPersistenceAdapter(wishlistJpaRepository);
    }

    @Test
    void add_savesEntityBuiltFromGivenUserIdAndTourId() {
        WishlistPersistenceAdapter adapter = newAdapter();

        adapter.add(5L, 2L);

        ArgumentCaptor<WishlistEntity> captor = ArgumentCaptor.forClass(WishlistEntity.class);
        verify(wishlistJpaRepository).saveAndFlush(captor.capture());
        WishlistEntity saved = captor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(5L);
        assertThat(saved.getTour().getId()).isEqualTo(2L);
    }

    @Test
    void add_duplicateEntry_swallowsConstraintViolation_insteadOfPropagating() {
        // Covers "thêm trùng không tạo bản ghi lặp": the (user_id, tour_id) unique constraint is
        // the actual source of truth here — this asserts the adapter treats that violation as a
        // no-op, not an error the caller has to handle.
        WishlistPersistenceAdapter adapter = newAdapter();
        when(wishlistJpaRepository.saveAndFlush(any(WishlistEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatCode(() -> adapter.add(5L, 2L)).doesNotThrowAnyException();
    }

    @Test
    void remove_delegatesToRepositoryDeleteQuery() {
        WishlistPersistenceAdapter adapter = newAdapter();

        adapter.remove(5L, 2L);

        verify(wishlistJpaRepository).deleteByUserIdAndTourId(5L, 2L);
    }

    private WishlistEntity entityWithTour(OffsetDateTime savedAt) {
        return WishlistEntity.builder()
                .id(1L)
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder()
                        .id(2L)
                        .title("Da Nang Tour")
                        .slug("da-nang-tour")
                        .destination(DestinationEntity.builder().id(10L).name("Da Nang").build())
                        .price(BigDecimal.valueOf(1000))
                        .discountPrice(BigDecimal.valueOf(900))
                        .durationDays(3)
                        .ratingAvg(BigDecimal.valueOf(4.5))
                        .reviewCount(12)
                        .featured(true)
                        .build())
                .createdAt(savedAt)
                .build();
    }

    @Test
    void findByUserId_mapsPageContent_intoTourAndSavedAt() {
        WishlistPersistenceAdapter adapter = newAdapter();
        OffsetDateTime savedAt = OffsetDateTime.now();
        WishlistEntity entity = entityWithTour(savedAt);
        Page<WishlistEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 12), 1);
        when(wishlistJpaRepository.findByUser_Id(eq(5L), any(Pageable.class))).thenReturn(page);

        PageResult<WishlistItem> result = adapter.findByUserId(5L, 1, 12);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(12);
        assertThat(result.data()).hasSize(1);
        WishlistItem item = result.data().get(0);
        assertThat(item.savedAt()).isEqualTo(savedAt);
        assertThat(item.tour().id()).isEqualTo(2L);
        assertThat(item.tour().title()).isEqualTo("Da Nang Tour");
        assertThat(item.tour().slug()).isEqualTo("da-nang-tour");
        assertThat(item.tour().destinationId()).isEqualTo(10L);
        assertThat(item.tour().destinationName()).isEqualTo("Da Nang");
        assertThat(item.tour().price()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(item.tour().discountPrice()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(item.tour().durationDays()).isEqualTo(3);
        assertThat(item.tour().featured()).isTrue();
    }

    @Test
    void findByUserId_tourWithoutDestination_mapsNullDestinationFields() {
        WishlistPersistenceAdapter adapter = newAdapter();
        WishlistEntity entity = WishlistEntity.builder()
                .id(1L)
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder().id(2L).title("Da Nang Tour").slug("da-nang-tour").build())
                .createdAt(OffsetDateTime.now())
                .build();
        Page<WishlistEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 12), 1);
        when(wishlistJpaRepository.findByUser_Id(eq(5L), any(Pageable.class))).thenReturn(page);

        WishlistItem item = adapter.findByUserId(5L, 1, 12).data().get(0);

        assertThat(item.tour().destinationId()).isNull();
        assertThat(item.tour().destinationName()).isNull();
    }

    @Test
    void findByUserId_passesZeroBasedPageIndexToPageRequest() {
        // Domain/API page is 1-based; Spring Data's Pageable is 0-based — page=1 must translate
        // to PageRequest index 0, not 1.
        WishlistPersistenceAdapter adapter = newAdapter();
        when(wishlistJpaRepository.findByUser_Id(eq(5L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.findByUserId(5L, 1, 12);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(wishlistJpaRepository).findByUser_Id(eq(5L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(12);
    }
}
