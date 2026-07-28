package com.tripgoapi.domain.exception;

import com.tripgoapi.domain.model.BookingStatus;

public class BookingCancellationNotAllowedException extends ConflictException {

    public BookingCancellationNotAllowedException(BookingStatus currentStatus) {
        super("Không thể hủy đơn ở trạng thái: " + currentStatus);
    }
}
