package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Map<Long, LocalDateTime> findLatestLoginAtByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return springDataLoginHistoryRepository.findLatestLoginAtByUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        SpringDataLoginHistoryRepository.LatestLoginAtProjection::getUserId,
                        SpringDataLoginHistoryRepository.LatestLoginAtProjection::getLatestLoginAt
                ));
    }
}
