package com.tripgoapi.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(T data, Long total, Integer page, Integer size) {

    public static <T> ApiResult<T> of(T data) {
        return new ApiResult<>(data, null, null, null);
    }

    public static <T> ApiResult<T> of(T data, long total, int page, int size) {
        return new ApiResult<>(data, total, page, size);
    }
}
