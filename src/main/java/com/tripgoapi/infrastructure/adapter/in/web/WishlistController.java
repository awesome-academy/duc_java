package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.AddToWishlistUseCase;
import com.tripgoapi.application.port.in.GetWishlistUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.RemoveFromWishlistUseCase;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.adapter.in.web.dto.AddWishlistRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.WishlistItemResponse;
import com.tripgoapi.infrastructure.mapper.WishlistWebMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Lưu / bỏ / xem danh sách tour yêu thích của người dùng đã đăng nhập")
public class WishlistController {

    private static final int MAX_PAGE_SIZE = 50;

    private final AddToWishlistUseCase addToWishlistUseCase;
    private final RemoveFromWishlistUseCase removeFromWishlistUseCase;
    private final GetWishlistUseCase getWishlistUseCase;
    private final WishlistWebMapper wishlistWebMapper;

    @Operation(
            summary = "Thêm tour vào wishlist",
            description = "userId lấy từ JWT, không nhận từ client. Thêm trùng một tour đã có sẵn trong wishlist không tạo bản ghi lặp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đã thêm (hoặc đã có sẵn) trong wishlist"),
            @ApiResponse(responseCode = "422", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> addToWishlist(
            @Valid @RequestBody AddWishlistRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        addToWishlistUseCase.addToWishlist(principal.userId(), request.tourId());
        return ResponseEntity.created(URI.create("/wishlist/" + request.tourId())).build();
    }

    @Operation(
            summary = "Bỏ tour khỏi wishlist",
            description = "Chỉ ảnh hưởng wishlist của chính người dùng đang đăng nhập. Idempotent: bỏ một tour không có trong wishlist vẫn trả về thành công."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đã bỏ (hoặc vốn không có) khỏi wishlist"),
            @ApiResponse(responseCode = "400", description = "tourId không hợp lệ (phải > 0)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{tourId}")
    public ResponseEntity<Void> removeFromWishlist(
            @Parameter(description = "ID tour") @PathVariable @Positive(message = "tourId phải > 0") Long tourId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        removeFromWishlistUseCase.removeFromWishlist(principal.userId(), tourId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Danh sách tour đã lưu",
            description = "Trả về các tour trong wishlist của người dùng hiện tại, có phân trang, mới lưu nhất trước."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "page/size không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ApiResult<List<WishlistItemResponse>> getWishlist(
            @Parameter(description = "Số trang, bắt đầu từ 1") @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page phải >= 1") int page,
            @Parameter(description = "Số phần tử mỗi trang, tối đa 50") @RequestParam(defaultValue = "12")
            @Min(value = 1, message = "size phải >= 1") @Max(value = MAX_PAGE_SIZE, message = "size phải <= 50") int size,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        PageResult<WishlistItem> result = getWishlistUseCase.getWishlist(principal.userId(), page, size);
        return ApiResult.of(
                wishlistWebMapper.toResponseList(result.data()),
                result.total(),
                result.page(),
                result.size()
        );
    }
}
