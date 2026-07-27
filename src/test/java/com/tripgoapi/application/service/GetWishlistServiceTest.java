package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.domain.model.WishlistItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWishlistServiceTest {

    @Mock
    private WishlistRepositoryInterface wishlistRepository;

    @Test
    void getWishlist_delegatesToRepositoryWithGivenPageAndSize() {
        GetWishlistService service = new GetWishlistService(wishlistRepository);
        Tour tour = new Tour(2L, "Da Nang Tour", "da-nang-tour", 1L, "Da Nang",
                BigDecimal.valueOf(1000), null, 3, BigDecimal.valueOf(4.5), 10, false);
        WishlistItem item = new WishlistItem(tour, OffsetDateTime.now());
        PageResult<WishlistItem> page = new PageResult<>(List.of(item), 1, 1, 12);
        when(wishlistRepository.findByUserId(5L, 1, 12)).thenReturn(page);

        PageResult<WishlistItem> result = service.getWishlist(5L, 1, 12);

        assertThat(result).isEqualTo(page);
        verify(wishlistRepository).findByUserId(5L, 1, 12);
    }
}
