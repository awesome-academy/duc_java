package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.domain.exception.EmailAlreadyExistsException;
import com.tripgoapi.domain.exception.InvalidCredentialsException;
import com.tripgoapi.domain.exception.TooManyLoginAttemptsException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpInputMessage httpInputMessage;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/tours/1");
    }

    @Test
    void notFoundExceptionReturns404WithDomainMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new TourNotFoundException(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Tour not found: id=1");
    }

    @Test
    void typeMismatchReturnsFriendlyMessageNamingTheParameter_notRawExceptionInternals() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "page", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String message = response.getBody().message();
        assertThat(message).contains("page");
        // must not leak internal exception message (class names, conversion details, etc.)
        assertThat(message).isNotEqualTo(ex.getMessage());
        assertThat(message).doesNotContain("java.lang.Integer");
    }

    @Test
    void missingParameterReturnsFriendlyMessageNamingTheParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("month", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("month");
        assertThat(response.getBody().message()).isNotEqualTo(ex.getMessage());
    }

    @Test
    void dateTimeParseExceptionReturnsGenericMessage_notRawParseDetails() {
        DateTimeParseException ex = new DateTimeParseException("Text '2026-13' could not be parsed", "2026-13", 5);

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Sai định dạng dữ liệu");
        assertThat(response.getBody().message()).doesNotContain("2026-13");
    }

    @Test
    void httpMessageNotReadableReturnsGenericMessage_notRawExceptionInternals() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("JSON parse error: Unexpected character at [Source: ...]", httpInputMessage);

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Sai định dạng dữ liệu");
        assertThat(response.getBody().message()).doesNotContain("JSON parse error");
    }

    @Test
    void conflictExceptionReturns409WithDomainMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new EmailAlreadyExistsException("jane@example.com"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("jane@example.com");
    }

    @Test
    void unauthorizedExceptionReturns401WithGenericCredentialsMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(new InvalidCredentialsException(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // Deliberately generic — must not reveal whether the email exists or only the password was wrong.
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
    }

    @Test
    void tooManyRequestsExceptionReturns429() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTooManyRequests(new TooManyLoginAttemptsException(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void constraintViolationReturnsAggregatedFriendlyMessages() {
        // Thrown when a service/component annotated with method-level Bean Validation
        // (e.g. a future @Validated application service) rejects its arguments.
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("page phải >= 1");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("page phải >= 1");
    }

    @Test
    void dataIntegrityViolationReturns409_notAnUnhandled500() {
        // Defense-in-depth: catches any unique-constraint violation that reaches the web layer
        // without being translated to a domain ConflictException at the persistence boundary.
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"users_email_key\"");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).doesNotContain("users_email_key");
    }

    @Test
    void unexpectedExceptionReturns500WithGenericMessage_notExceptionInternals() {
        RuntimeException ex = new RuntimeException("db connection to internal-host:5432 refused");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("internal-host");
    }
}
