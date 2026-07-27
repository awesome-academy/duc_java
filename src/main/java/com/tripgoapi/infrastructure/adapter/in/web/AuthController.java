package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.in.GetCurrentUserUseCase;
import com.tripgoapi.application.port.in.LoginCommand;
import com.tripgoapi.application.port.in.LoginUseCase;
import com.tripgoapi.application.port.in.LogoutUseCase;
import com.tripgoapi.application.port.in.RefreshTokenUseCase;
import com.tripgoapi.application.port.in.RegisterUserCommand;
import com.tripgoapi.application.port.in.RegisterUserUseCase;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ApiResult;
import com.tripgoapi.infrastructure.adapter.in.web.dto.AuthTokenResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.LoginRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.RefreshTokenRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.tripgoapi.infrastructure.adapter.in.web.dto.UserResponse;
import com.tripgoapi.infrastructure.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng ký, đăng nhập, làm mới token, đăng xuất, thông tin tài khoản")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UserWebMapper userWebMapper;

    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới. Không tự đăng nhập — gọi /auth/login sau khi đăng ký.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công"),
            @ApiResponse(responseCode = "409", description = "Email đã được đăng ký",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResult<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.fullName(), request.email(), request.password(), request.phone());
        UserResponse response = userWebMapper.toResponse(registerUserUseCase.register(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.of(response));
    }

    @Operation(summary = "Đăng nhập", description = "Trả cặp access token (ngắn hạn) + refresh token (dài hạn, dùng để lấy access token mới qua /auth/refresh).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ApiResult<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ApiResult.of(toResponse(token));
    }

    @Operation(summary = "Làm mới access token", description = "Đổi refresh token còn hạn lấy cặp access + refresh token mới (refresh token cũ bị vô hiệu ngay — rotation).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ, hết hạn, hoặc đã bị thu hồi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ApiResult<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthToken token = refreshTokenUseCase.refresh(request.refreshToken());
        return ApiResult.of(toResponse(token));
    }

    @Operation(summary = "Đăng xuất", description = "Thu hồi refresh token được gửi lên — access token hiện tại vẫn dùng được tới khi hết hạn (JWT stateless).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đăng xuất thành công")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Thông tin tài khoản hiện tại", description = "Yêu cầu access token hợp lệ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Thiếu hoặc sai access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ApiResult<UserResponse> getCurrentUser(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserResponse response = userWebMapper.toResponse(getCurrentUserUseCase.getCurrentUser(principal.userId()));
        return ApiResult.of(response);
    }

    private AuthTokenResponse toResponse(AuthToken token) {
        return new AuthTokenResponse(token.accessToken(), token.refreshToken(), token.tokenType(), token.expiresInSeconds());
    }
}
