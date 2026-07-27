package com.tripgoapi.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityErrorResponseWriterTest {

    private final SecurityErrorResponseWriter writer = new SecurityErrorResponseWriter(JsonMapper.builder().build());

    @Test
    void write_setsStatusContentTypeAndJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(request, response, HttpStatus.UNAUTHORIZED, "Yêu cầu xác thực");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("Yêu cầu xác thực");
        assertThat(body).contains("/auth/me");
    }
}
