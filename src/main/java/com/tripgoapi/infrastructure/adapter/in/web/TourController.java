package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.GetToursUseCase;
import com.tripgoapi.application.port.in.GetTourAvailabilityUseCase;
import com.tripgoapi.application.port.in.GetTourDetailUseCase;
import com.tripgoapi.application.port.in.GetTourReviewsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourReviewsResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.application.port.in.TourSortOption;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourAvailabilityResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourCardResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourDetailResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourReviewsResponse;
import com.tripgoapi.infrastructure.mapper.ReviewWebMapper;
import com.tripgoapi.infrastructure.mapper.TourAvailabilityWebMapper;
import com.tripgoapi.infrastructure.mapper.TourDetailWebMapper;
import com.tripgoapi.infrastructure.mapper.TourWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@Tag(name = "Tours", description = "Tìm kiếm, chi tiết, lịch khởi hành và đánh giá tour")
public class TourController {

    private static final int DEFAULT_REVIEW_PAGE_SIZE = 10;
    private static final int MAX_REVIEW_PAGE_SIZE = 50;

    private final GetToursUseCase getToursUseCase;
    private final GetTourDetailUseCase getTourDetailUseCase;
    private final GetTourAvailabilityUseCase getTourAvailabilityUseCase;
    private final GetTourReviewsUseCase getTourReviewsUseCase;
    private final TourWebMapper tourWebMapper;
    private final TourDetailWebMapper tourDetailWebMapper;
    private final TourAvailabilityWebMapper tourAvailabilityWebMapper;
    private final ReviewWebMapper reviewWebMapper;

    @Operation(
            summary = "Tìm kiếm / lọc / sắp xếp / phân trang tour",
            description = "Lọc theo từ khóa, điểm đến, loại hình, khoảng giá, thời lượng, đánh giá tối thiểu. "
                    + "Toàn bộ lọc/sắp xếp/phân trang thực hiện ở DB, chỉ trả tour đang ACTIVE."
    )
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ApiResult<List<TourCardResponse>> getTours(
            @Parameter(description = "Từ khóa tìm theo tiêu đề tour") @RequestParam(required = false) String q,
            @Parameter(description = "Slug điểm đến", example = "da-nang") @RequestParam(required = false) String destination,
            @Parameter(description = "Slug loại hình tour", example = "bien") @RequestParam(required = false) String category,
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Số ngày tour (khớp chính xác)") @RequestParam(required = false) Integer duration,
            @Parameter(description = "Đánh giá tối thiểu, thang 5") @RequestParam(required = false) BigDecimal rating,
            @Parameter(description = "Chỉ lấy tour nổi bật") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Kiểu sắp xếp: newest, price_asc, price_desc, rating_desc. Mặc định newest",
                    example = "price_asc") @RequestParam(required = false) String sort,
            @Parameter(description = "Số trang, bắt đầu từ 1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số phần tử mỗi trang, tối đa 50") @RequestParam(defaultValue = "12") int limit
    ) {
        TourSearchQuery query = new TourSearchQuery(
                q, destination, category, minPrice, maxPrice, duration, rating, featured,
                parseSort(sort), page, limit
        );

        PageResult<Tour> result = getToursUseCase.searchTours(query);

        return ApiResult.of(
                tourWebMapper.toResponseList(result.data()),
                result.total(),
                result.page(),
                result.size()
        );
    }

    @Operation(
            summary = "Chi tiết tour",
            description = "Trả đầy đủ thông tin tour: ảnh, mô tả, lịch trình theo ngày, highlights, bao gồm/không bao gồm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ApiResult<TourDetailResponse> getTourDetail(@Parameter(description = "ID tour") @PathVariable Long id) {
        return ApiResult.of(tourDetailWebMapper.toResponse(getTourDetailUseCase.getTourDetail(id)));
    }

    @Operation(
            summary = "Lịch khởi hành & số chỗ còn",
            description = "Trả danh sách ngày khởi hành trong tháng chỉ định kèm số chỗ còn lại."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sai định dạng tháng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/availability")
    public ApiResult<List<TourAvailabilityResponse>> getAvailability(
            @Parameter(description = "ID tour") @PathVariable Long id,
            @Parameter(description = "Tháng cần xem, định dạng yyyy-MM. Mặc định tháng hiện tại", example = "2026-08")
            @RequestParam(required = false) String month
    ) {
        YearMonth targetMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return ApiResult.of(tourAvailabilityWebMapper.toResponseList(getTourAvailabilityUseCase.getAvailability(id, targetMonth)));
    }

    @Operation(
            summary = "Đánh giá của tour",
            description = "Trả danh sách đánh giá đã phân trang kèm điểm đánh giá trung bình của tour."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/reviews")
    public TourReviewsResponse getReviews(
            @Parameter(description = "ID tour") @PathVariable Long id,
            @Parameter(description = "Số trang, bắt đầu từ 1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số đánh giá mỗi trang, tối đa 50") @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_REVIEW_PAGE_SIZE);
        if (size < 1) {
            safeSize = DEFAULT_REVIEW_PAGE_SIZE;
        }

        TourReviewsResult result = getTourReviewsUseCase.getReviews(id, safePage, safeSize);

        return new TourReviewsResponse(
                reviewWebMapper.toResponseList(result.reviews().data()),
                result.reviews().total(),
                result.reviews().page(),
                result.reviews().size(),
                result.averageRating()
        );
    }

    private TourSortOption parseSort(String sort) {
        if (sort == null) {
            return null;
        }
        try {
            return TourSortOption.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
