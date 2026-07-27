package com.tripgoapi.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.in.GetCurrentUserUseCase;
import com.tripgoapi.application.port.in.LoginCommand;
import com.tripgoapi.application.port.in.LoginUseCase;
import com.tripgoapi.application.port.in.LogoutUseCase;
import com.tripgoapi.application.port.in.RefreshTokenUseCase;
import com.tripgoapi.application.port.in.RegisterUserUseCase;
import com.tripgoapi.infrastructure.mapper.UserWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private LoginUseCase loginUseCase;
    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;
    @Mock
    private LogoutUseCase logoutUseCase;
    @Mock
    private GetCurrentUserUseCase getCurrentUserUseCase;
    @Mock
    private UserWebMapper userWebMapper;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(
                registerUserUseCase, loginUseCase, refreshTokenUseCase, logoutUseCase,
                getCurrentUserUseCase, userWebMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_passesCallersRemoteAddressAsIpAddress() throws Exception {
        when(loginUseCase.login(any(LoginCommand.class)))
                .thenReturn(new AuthToken("access", "refresh", "Bearer", 3600));
        String body = objectMapper.writeValueAsString(new LoginRequestBody("jane@example.com", "secret"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.5");
                            return request;
                        }))
                .andExpect(status().isOk());

        ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(loginUseCase).login(captor.capture());
        LoginCommand command = captor.getValue();
        assertThat(command.email()).isEqualTo("jane@example.com");
        assertThat(command.password()).isEqualTo("secret");
        assertThat(command.ipAddress()).isEqualTo("203.0.113.5");
    }

    private record LoginRequestBody(String email, String password) {
    }
}
