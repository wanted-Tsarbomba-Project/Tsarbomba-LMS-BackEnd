package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public void deleteByUserId(Long userId) {
        springDataRefreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.from(refreshToken);
        return springDataRefreshTokenRepository.save(entity).toDomain();
    }
}