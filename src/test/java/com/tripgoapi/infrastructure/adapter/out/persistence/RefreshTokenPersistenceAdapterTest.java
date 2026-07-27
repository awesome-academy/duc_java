package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.StoredRefreshToken;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenPersistenceAdapterTest {

    @Mock
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    private RefreshTokenPersistenceAdapter adapter;

    private RefreshTokenPersistenceAdapter newAdapter() {
        return new RefreshTokenPersistenceAdapter(refreshTokenJpaRepository);
    }

    @Test
    void revokeIfActive_oneRowUpdated_returnsTrue() {
        // The whole point of the atomic-revoke fix: "1 row affected" is the DB's own signal
        // that THIS call won the race to flip revoked=false -> true.
        adapter = newAdapter();
        when(refreshTokenJpaRepository.revokeIfNotRevoked("hash")).thenReturn(1);

        assertThat(adapter.revokeIfActive("hash")).isTrue();
    }

    @Test
    void revokeIfActive_zeroRowsUpdated_returnsFalse() {
        // Zero rows affected means the WHERE revoked = false predicate didn't match — someone
        // else already revoked it (or it never existed). Must not be reported as a success.
        adapter = newAdapter();
        when(refreshTokenJpaRepository.revokeIfNotRevoked("hash")).thenReturn(0);

        assertThat(adapter.revokeIfActive("hash")).isFalse();
    }

    @Test
    void save_persistsEntityWithGivenUserTokenHashAndExpiry() {
        adapter = newAdapter();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(14);

        adapter.save(5L, "hash", expiresAt);

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenJpaRepository).save(captor.capture());
        RefreshTokenEntity saved = captor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(5L);
        assertThat(saved.getTokenHash()).isEqualTo("hash");
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(saved.isRevoked()).isFalse();
    }

    @Test
    void findByTokenHash_mapsToPortRecord() {
        adapter = newAdapter();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(9L).user(UserEntity.builder().id(5L).build())
                .tokenHash("hash").expiresAt(expiresAt).revoked(true)
                .build();
        when(refreshTokenJpaRepository.findByTokenHash("hash")).thenReturn(Optional.of(entity));

        Optional<StoredRefreshToken> result = adapter.findByTokenHash("hash");

        assertThat(result).contains(new StoredRefreshToken(9L, 5L, expiresAt, true));
    }

    @Test
    void revokeAllForUser_delegatesToRepository() {
        adapter = newAdapter();

        adapter.revokeAllForUser(5L);

        verify(refreshTokenJpaRepository).revokeAllForUser(5L);
    }

    @Test
    void deleteExpiredOrStaleRevoked_delegatesToRepositoryAndReturnsCount() {
        adapter = newAdapter();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime revokedRetentionBefore = now.minusDays(7);
        when(refreshTokenJpaRepository.deleteExpiredOrStaleRevoked(now, revokedRetentionBefore)).thenReturn(3);

        int deleted = adapter.deleteExpiredOrStaleRevoked(now, revokedRetentionBefore);

        assertThat(deleted).isEqualTo(3);
        verify(refreshTokenJpaRepository).deleteExpiredOrStaleRevoked(now, revokedRetentionBefore);
    }
}
