package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import com.tripgoapi.application.port.out.StoredRefreshToken;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryInterface {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public void save(Long userId, String tokenHash, OffsetDateTime expiresAt) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .createdAt(OffsetDateTime.now())
                .build();

        refreshTokenJpaRepository.save(entity);
    }

    @Override
    public Optional<StoredRefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .map(e -> new StoredRefreshToken(e.getId(), e.getUser().getId(), e.getExpiresAt(), e.isRevoked()));
    }

    @Override
    public boolean revokeIfActive(String tokenHash) {
        return refreshTokenJpaRepository.revokeIfNotRevoked(tokenHash) > 0;
    }

    @Override
    public void revokeAllForUser(Long userId) {
        refreshTokenJpaRepository.revokeAllForUser(userId);
    }

    @Override
    public int deleteExpiredOrStaleRevoked(OffsetDateTime now, OffsetDateTime revokedRetentionBefore) {
        return refreshTokenJpaRepository.deleteExpiredOrStaleRevoked(now, revokedRetentionBefore);
    }
}
