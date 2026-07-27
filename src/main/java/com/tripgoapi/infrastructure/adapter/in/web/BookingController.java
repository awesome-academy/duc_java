package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.CancelBookingUseCase;
import com.tripgoapi.application.port.in.CreateBookingCommand;
import com.tripgoapi.application.port.in.CreateBookingUseCase;
import com.tripgoapi.application.port.in.GetBookingDetailUseCase;
import com.tripgoapi.application.port.in.GetBookingsUseCase;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.BookingResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.CreateBookingRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.mapper.BookingWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bookings", description = "Đặt tour & xem đơn của người dùng đã đăng nhập")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetBookingsUseCase getBookingsUseCase;
    private final GetBookingDetailUseCase getBookingDetailUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final BookingWebMapper bookingWebMapper;

    @Operation(
            summary = "Tạo đơn đặt tour",
            description = "Server tự tính totalPrice theo giá tour & số khách, tự sinh mã đơn, "
                    + "và gắn userId từ JWT — không tin totalPrice/userId do client gửi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đặt tour thành công"),
            @ApiResponse(responseCode = "422", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tour hoặc ngày khởi hành",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ngày khởi hành đã hết chỗ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResult<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CreateBookingCommand command = new CreateBookingCommand(
                request.idempotencyKey(),
                principal.userId(),
                request.tourId(),
                request.date(),
                request.adults(),
                request.children(),
                request.contact().name(),
                request.contact().email(),
                request.contact().phone()
        );
        Booking booking = createBookingUseCase.createBooking(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.of(bookingWebMapper.toResponse(booking)));
    }

    @Operation(
            summary = "Danh sách đơn đặt tour của tôi",
            description = "Lọc theo trạng thái qua query, vd: ?status=pending. Bỏ qua nếu không truyền hoặc giá trị không hợp lệ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ApiResult<List<BookingResponse>> getBookings(
            @Parameter(description = "Lọc theo trạng thái đơn", example = "pending")
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<Booking> bookings = getBookingsUseCase.getBookingsForUser(principal.userId(), parseStatus(status));
        return ApiResult.of(bookingWebMapper.toResponseList(bookings));
    }

    @Operation(summary = "Chi tiết đơn đặt tour", description = "Chỉ trả về đơn thuộc về chính người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Đơn không thuộc về người dùng hiện tại",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ApiResult<BookingResponse> getBookingDetail(
            @Parameter(description = "ID đơn") @PathVariable @Positive(message = "id phải > 0") Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Booking booking = getBookingDetailUseCase.getBookingDetail(id, principal.userId());
        return ApiResult.of(bookingWebMapper.toResponse(booking));
    }

    @Operation(
            summary = "Hủy đơn đặt tour",
            description = "Chỉ chủ đơn mới hủy được, và chỉ khi đơn đang ở trạng thái PENDING hoặc CONFIRMED. "
                    + "Trả lại số chỗ đã giữ cho ngày khởi hành tương ứng."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Đơn không thuộc về người dùng hiện tại",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Đơn đang ở trạng thái không thể hủy",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/cancel")
    public ApiResult<BookingResponse> cancelBooking(
            @Parameter(description = "ID đơn") @PathVariable @Positive(message = "id phải > 0") Long id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        Booking booking = cancelBookingUseCase.cancelBooking(id, principal.userId());
        return ApiResult.of(bookingWebMapper.toResponse(booking));
    }

    private BookingStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
