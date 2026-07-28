package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.CreateReviewCommand;
import com.tripgoapi.application.port.in.CreateReviewUseCase;
import com.tripgoapi.application.port.in.GetTourReviewsUseCase;
import com.tripgoapi.application.port.in.TourReviewsResult;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.CreateReviewRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ReviewResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourReviewsResponse;
import com.tripgoapi.infrastructure.mapper.ReviewWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Split out of TourController: every other endpoint there is an unauthenticated GET (tour
// catalog browsing), while POST here is the one authenticated write action under /tours/**.
// Keeping it separate means SecurityConfig's GET-only public matcher for /tours/** reads as
// "this whole class is public" without having to reason about one write method mixed in.
@RestController
@RequestMapping("/tours/{tourId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Tour Reviews", description = "Xem và tạo đánh giá tour")
public class TourReviewController {

    private static final int MAX_PAGE_SIZE = 50;

    private final GetTourReviewsUseCase getTourReviewsUseCase;
    private final CreateReviewUseCase createReviewUseCase;
    private final ReviewWebMapper reviewWebMapper;

    @Operation(
            summary = "Đánh giá của tour",
            description = "Trả danh sách đánh giá đã phân trang kèm điểm đánh giá trung bình của tour."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public TourReviewsResponse getReviews(
            @Parameter(description = "ID tour") @PathVariable @Positive(message = "tourId phải > 0") Long tourId,
            @Parameter(description = "Số trang, bắt đầu từ 1") @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page phải >= 1") int page,
            @Parameter(description = "Số đánh giá mỗi trang, tối đa 50") @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size phải >= 1") @Max(value = MAX_PAGE_SIZE, message = "size phải <= 50") int size
    ) {
        TourReviewsResult result = getTourReviewsUseCase.getReviews(tourId, page, size);

        return new TourReviewsResponse(
                reviewWebMapper.toResponseList(result.reviews().data()),
                result.reviews().total(),
                result.reviews().page(),
                result.reviews().size(),
                result.averageRating()
        );
    }

    // No @Positive on tourId (unlike getReviews above): combined with @Valid on the body below,
    // Spring routes the whole method through unified method validation and rating violations
    // would surface as 400 instead of 422. A non-positive/non-existent tourId still 404s via
    // TourNotFoundException from the service, just one hop later.
    @Operation(
            summary = "Đánh giá tour",
            description = "Chỉ user đã đăng nhập và đã đặt tour này (booking CONFIRMED/COMPLETED) mới được đánh giá; "
                    + "mỗi user chỉ đánh giá một tour một lần. Sau khi tạo, rating trung bình và số lượt đánh giá của tour được cập nhật ngay."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đánh giá thành công"),
            @ApiResponse(responseCode = "422", description = "Dữ liệu không hợp lệ (rating ngoài khoảng 1-5)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Chưa đặt/hoàn thành tour này nên không thể đánh giá",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Đã đánh giá tour này rồi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResult<ReviewResponse>> createReview(
            @Parameter(description = "ID tour") @PathVariable Long tourId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CreateReviewCommand command = new CreateReviewCommand(principal.userId(), tourId, request.rating(), request.comment());
        Review review = createReviewUseCase.createReview(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.of(reviewWebMapper.toResponse(review)));
    }
}
