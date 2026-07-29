package com.tripgoapi.application.port.in;

public interface RemoveFromWishlistUseCase {

    /**
     * Idempotent: removing a tour that isn't in the user's wishlist is a no-op, not an error.
     */
    void removeFromWishlist(Long userId, Long tourId);
}
