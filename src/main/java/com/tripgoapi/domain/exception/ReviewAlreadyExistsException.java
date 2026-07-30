package com.tripgoapi.domain.exception;

public class ReviewAlreadyExistsException extends ConflictException {

    public ReviewAlreadyExistsException(Long tourId) {
        super("Bạn đã đánh giá tour này rồi: tourId=" + tourId);
    }
}
