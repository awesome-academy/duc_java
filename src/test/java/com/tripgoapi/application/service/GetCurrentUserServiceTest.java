package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.UserNotFoundException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserServiceTest {

    @Mock
    private UserRepositoryInterface userRepository;

    @Test
    void returnsUser_whenFound() {
        GetCurrentUserService service = new GetCurrentUserService(userRepository);
        User user = new User(1L, "Jane", "jane@example.com", null, Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.getCurrentUser(1L)).isSameAs(user);
    }

    @Test
    void throwsUserNotFound_whenMissing() {
        GetCurrentUserService service = new GetCurrentUserService(userRepository);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser(2L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCurrentUserIsAnnotatedReadOnlyTransactional() throws NoSuchMethodException {
        Method method = GetCurrentUserService.class.getMethod("getCurrentUser", Long.class);
        Transactional annotation = method.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }
}
