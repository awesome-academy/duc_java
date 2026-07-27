package com.tripgoapi.infrastructure.adapter.out.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAccessDeniedHandlerTest {

    @Mock
    private SecurityErrorResponseWriter responseWriter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    void handle_delegatesToResponseWriterWith403() throws Exception {
        JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(responseWriter);

        handler.handle(request, response, new AccessDeniedException("denied"));

        verify(responseWriter).write(request, response, HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập tài nguyên này");
    }
}
