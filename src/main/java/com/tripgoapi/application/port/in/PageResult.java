package com.tripgoapi.application.port.in;

import java.util.List;

public record PageResult<T>(List<T> data, long total, int page, int size) {
}
