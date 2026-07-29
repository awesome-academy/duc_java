package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemoveFromWishlistServiceTest {

    @Mock
    private WishlistRepositoryInterface wishlistRepository;

    @Test
    void removeFromWishlist_delegatesToRepository() {
        RemoveFromWishlistService service = new RemoveFromWishlistService(wishlistRepository);

        service.removeFromWishlist(5L, 2L);

        verify(wishlistRepository).remove(5L, 2L);
    }
}
