package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetWishlistUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.model.WishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWishlistService implements GetWishlistUseCase {

    private final WishlistRepositoryInterface wishlistRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<WishlistItem> getWishlist(Long userId, int page, int size) {
        return wishlistRepository.findByUserId(userId, page, size);
    }
}
