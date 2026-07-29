package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AddToWishlistUseCase;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddToWishlistService implements AddToWishlistUseCase {

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final WishlistRepositoryInterface wishlistRepository;

    @Override
    @Transactional
    public void addToWishlist(Long userId, Long tourId) {
        if (!tourDetailRepository.existsActiveTour(tourId)) {
            throw new TourNotFoundException(tourId);
        }
        wishlistRepository.add(userId, tourId);
    }
}
