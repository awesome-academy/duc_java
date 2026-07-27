package com.tripgoapi.infrastructure.adapter.out.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private SecurityErrorResponseWriter responseWriter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    void commence_delegatesToResponseWriterWith401() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(responseWriter);

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        verify(responseWriter).write(request, response, HttpStatus.UNAUTHORIZED,
                "Yêu cầu xác thực — thiếu hoặc sai access token");
    }
}
