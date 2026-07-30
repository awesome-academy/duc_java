package com.tripgoapi.domain.exception;

public class ReviewNotAllowedException extends ForbiddenException {

    public ReviewNotAllowedException(Long tourId) {
        // Matches the real rule in CreateReviewService: a CONFIRMED booking whose departure date
        // hasn't passed yet does not qualify, so the message must not imply booking alone suffices.
        super("Bạn cần đặt tour này và tour phải đã khởi hành thì mới có thể đánh giá: tourId=" + tourId);
    }
}
