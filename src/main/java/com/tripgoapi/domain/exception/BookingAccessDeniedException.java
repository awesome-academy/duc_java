package com.tripgoapi.domain.exception;

public class BookingAccessDeniedException extends ForbiddenException {

    public BookingAccessDeniedException(Long bookingId) {
        super("Bạn không có quyền truy cập đơn đặt tour: id=" + bookingId);
    }
}
