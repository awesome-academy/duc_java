package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.WishlistItem;

public interface GetWishlistUseCase {

    /**
     * @return a page of tours the user has saved, newest saved first
     */
    PageResult<WishlistItem> getWishlist(Long userId, int page, int size);
}
