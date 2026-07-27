package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.GetDestinationsUseCase;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.DestinationCardResponse;
import com.tripgoapi.infrastructure.mapper.DestinationWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
@Tag(name = "Destinations", description = "Điểm đến cho trang chủ")
public class DestinationController {

    private final GetDestinationsUseCase getDestinationsUseCase;
    private final DestinationWebMapper destinationWebMapper;

    @Operation(
            summary = "Danh sách điểm đến",
            description = "Trả danh sách điểm đến kèm số lượng tour đang mở bán (status ACTIVE) của mỗi điểm đến."
    )
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ApiResult<List<DestinationCardResponse>> getDestinations() {
        return ApiResult.of(destinationWebMapper.toResponseList(getDestinationsUseCase.getDestinations()));
    }
}
