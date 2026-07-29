package com.tripgoapi.domain.exception;

import com.tripgoapi.domain.model.BookingStatus;

public class BookingStatusTransitionNotAllowedException extends ConflictException {

    public BookingStatusTransitionNotAllowedException(BookingStatus currentStatus, BookingStatus targetStatus) {
        super("Không thể chuyển đơn từ trạng thái " + currentStatus + " sang " + targetStatus);
    }
}
