package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddToWishlistServiceTest {

    private static final Long USER_ID = 5L;
    private static final Long TOUR_ID = 2L;

    @Mock
    private TourDetailRepositoryInterface tourDetailRepository;
    @Mock
    private WishlistRepositoryInterface wishlistRepository;

    private AddToWishlistService service;

    @Test
    void tourNotFound_throwsTourNotFoundException_neverAdds() {
        service = new AddToWishlistService(tourDetailRepository, wishlistRepository);
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.addToWishlist(USER_ID, TOUR_ID))
                .isInstanceOf(TourNotFoundException.class);

        verify(wishlistRepository, never()).add(USER_ID, TOUR_ID);
    }

    @Test
    void tourExists_delegatesAddToRepository() {
        service = new AddToWishlistService(tourDetailRepository, wishlistRepository);
        when(tourDetailRepository.existsActiveTour(TOUR_ID)).thenReturn(true);

        service.addToWishlist(USER_ID, TOUR_ID);

        verify(wishlistRepository).add(USER_ID, TOUR_ID);
    }
}
