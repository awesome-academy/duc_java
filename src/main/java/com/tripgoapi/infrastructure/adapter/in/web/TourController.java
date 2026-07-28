package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.GetToursUseCase;
import com.tripgoapi.application.port.in.GetTourAvailabilityUseCase;
import com.tripgoapi.application.port.in.GetTourDetailUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.application.port.in.TourSortOption;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourAvailabilityResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourCardResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourDetailResponse;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
@Tag(name = "Tours", description = "Tìm kiếm, chi tiết và lịch khởi hành tour")
public class TourController {

    private static final int MAX_PAGE_SIZE = 50;

    private final GetToursUseCase getToursUseCase;
    private final GetTourDetailUseCase getTourDetailUseCase;
    private final GetTourAvailabilityUseCase getTourAvailabilityUseCase;
    private final TourWebMapper tourWebMapper;
    private final TourDetailWebMapper tourDetailWebMapper;
    private final TourAvailabilityWebMapper tourAvailabilityWebMapper;

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
            @Parameter(description = "Giá tối thiểu") @RequestParam(required = false)
            @PositiveOrZero(message = "minPrice phải >= 0") BigDecimal minPrice,
            @Parameter(description = "Giá tối đa") @RequestParam(required = false)
            @PositiveOrZero(message = "maxPrice phải >= 0") BigDecimal maxPrice,
            @Parameter(description = "Số ngày tour (khớp chính xác)") @RequestParam(required = false)
            @Positive(message = "duration phải > 0") Integer duration,
            @Parameter(description = "Đánh giá tối thiểu, thang 5") @RequestParam(required = false)
            @DecimalMin(value = "0", message = "rating phải >= 0")
            @DecimalMax(value = "5", message = "rating phải <= 5") BigDecimal rating,
            @Parameter(description = "Chỉ lấy tour nổi bật") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Kiểu sắp xếp: newest, price_asc, price_desc, rating_desc. Mặc định newest",
                    example = "price_asc") @RequestParam(required = false) String sort,
            @Parameter(description = "Số trang, bắt đầu từ 1") @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page phải >= 1") int page,
            @Parameter(description = "Số phần tử mỗi trang, tối đa 50") @RequestParam(defaultValue = "12")
            @Min(value = 1, message = "limit phải >= 1") @Max(value = MAX_PAGE_SIZE, message = "limit phải <= 50") int limit
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
    public ApiResult<TourDetailResponse> getTourDetail(
            @Parameter(description = "ID tour") @PathVariable @Positive(message = "id phải > 0") Long id) {
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
            @Parameter(description = "ID tour") @PathVariable @Positive(message = "id phải > 0") Long id,
            @Parameter(description = "Tháng cần xem, định dạng yyyy-MM. Mặc định tháng hiện tại", example = "2026-08")
            @RequestParam(required = false) String month
    ) {
        YearMonth targetMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return ApiResult.of(tourAvailabilityWebMapper.toResponseList(getTourAvailabilityUseCase.getAvailability(id, targetMonth)));
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
