package com.tripgoapi.domain.exception;

public class DestinationInUseException extends ConflictException {

    public DestinationInUseException(long tourCount) {
        super("Không thể xóa điểm đến: đang có " + tourCount + " tour thuộc điểm đến này");
    }
}
