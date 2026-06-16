package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginHistoryRepositoryAdapter implements LoginHistoryRepository {

    private final SpringDataLoginHistoryRepository springDataLoginHistoryRepository;

    @Override
    public LoginHistory save(LoginHistory loginHistory) {
        LoginHistoryJpaEntity entity = LoginHistoryJpaEntity.from(loginHistory);
        return springDataLoginHistoryRepository.save(entity).toDomain();
    }

    @Override
    public Optional<LoginHistory> findLatestByUserId(Long userId) {
        return springDataLoginHistoryRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(LoginHistoryJpaEntity::toDomain);
    }
}
