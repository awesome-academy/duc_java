package com.tripgoapi.domain.exception;

public class DestinationNotFoundException extends NotFoundException {

    public DestinationNotFoundException(Long id) {
        super("Không tìm thấy điểm đến: id=" + id);
    }
}
