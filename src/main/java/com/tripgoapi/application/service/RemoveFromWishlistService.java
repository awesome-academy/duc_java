package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.RemoveFromWishlistUseCase;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveFromWishlistService implements RemoveFromWishlistUseCase {

    private final WishlistRepositoryInterface wishlistRepository;

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long tourId) {
        wishlistRepository.remove(userId, tourId);
    }
}
