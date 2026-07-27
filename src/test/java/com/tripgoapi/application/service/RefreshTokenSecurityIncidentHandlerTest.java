package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSecurityIncidentHandlerTest {

    @Mock
    private RefreshTokenRepositoryInterface refreshTokenRepository;

    @Test
    void revokeAllSessions_delegatesToRepository() {
        RefreshTokenSecurityIncidentHandler handler = new RefreshTokenSecurityIncidentHandler(refreshTokenRepository);

        handler.revokeAllSessions(7L);

        verify(refreshTokenRepository).revokeAllForUser(7L);
    }

    @Test
    void revokeAllSessionsRunsInItsOwnTransaction_soItSurvivesTheCallerRollingBack() throws NoSuchMethodException {
        Method method = RefreshTokenSecurityIncidentHandler.class.getDeclaredMethod("revokeAllSessions", Long.class);
        Transactional annotation = method.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }
}
