package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.domain.exception.ConflictException;
import com.tripgoapi.domain.exception.FileStorageException;
import com.tripgoapi.domain.exception.NotFoundException;
import com.tripgoapi.domain.exception.UnprocessableException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * HTML counterpart of {@code GlobalExceptionHandler}: the admin portal renders pages, so failures
 * must come back as a styled error view rather than the JSON body the REST advice produces.
 */
@ControllerAdvice(basePackages = "com.tripgoapi.infrastructure.adapter.in.admin")
public class AdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminExceptionHandler.class);

    private static final String ERROR_VIEW = "admin/error";

    /**
     * Rethrown, not rendered: {@code ExceptionTranslationFilter} must be the one to see this so
     * it can apply the {@code accessDeniedPage} configured in {@code AdminSecurityConfig}. Without
     * this handler, the catch-all {@link #handleUnexpected} below would swallow it first and turn
     * every authorization failure into a generic 500 page instead of the proper 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) throws AccessDeniedException {
        throw ex;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model) {
        return render(model, "Không tìm thấy dữ liệu", ex.getMessage());
    }

    @ExceptionHandler({ConflictException.class, UnprocessableException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(RuntimeException ex, Model model) {
        return render(model, "Không thực hiện được thao tác", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public String handleUploadTooLarge(Model model) {
        return render(model, "Ảnh quá lớn", "Dung lượng ảnh vượt quá giới hạn cho phép, vui lòng chọn ảnh nhỏ hơn.");
    }

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleFileStorage(FileStorageException ex, Model model, HttpServletRequest request) {
        log.error("File storage failure at {}", request.getRequestURI(), ex);
        return render(model, "Lỗi lưu ảnh", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled admin exception at {}", request.getRequestURI(), ex);
        // Deliberately generic: stack traces and internal messages don't belong on a rendered page.
        return render(model, "Đã có lỗi xảy ra", "Vui lòng thử lại hoặc liên hệ quản trị hệ thống.");
    }

    private String render(Model model, String title, String detail) {
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorDetail", detail);
        return ERROR_VIEW;
    }
}
