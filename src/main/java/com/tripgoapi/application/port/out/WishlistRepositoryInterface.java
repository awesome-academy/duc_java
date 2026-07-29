package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.model.WishlistItem;

public interface WishlistRepositoryInterface {

    /**
     * Idempotent: relies on the (user_id, tour_id) unique constraint, so a duplicate add
     * is silently absorbed instead of raising a conflict.
     */
    void add(Long userId, Long tourId);

    /**
     * Idempotent: deleting an entry that doesn't exist is a no-op.
     */
    void remove(Long userId, Long tourId);

    /**
     * @return a page of tours saved by the user, newest saved first
     */
    PageResult<WishlistItem> findByUserId(Long userId, int page, int size);
}
