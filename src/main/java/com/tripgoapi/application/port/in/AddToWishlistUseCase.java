package com.tripgoapi.application.port.in;

public interface AddToWishlistUseCase {

    /**
     * Idempotent: adding a tour already in the user's wishlist is a no-op, not an error.
     */
    void addToWishlist(Long userId, Long tourId);
}
