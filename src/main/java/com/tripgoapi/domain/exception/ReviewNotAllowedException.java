package com.tripgoapi.domain.exception;

public class ReviewNotAllowedException extends ForbiddenException {

    public ReviewNotAllowedException(Long tourId) {
        super("Bạn cần đặt và hoàn thành tour này trước khi đánh giá: tourId=" + tourId);
    }
}
