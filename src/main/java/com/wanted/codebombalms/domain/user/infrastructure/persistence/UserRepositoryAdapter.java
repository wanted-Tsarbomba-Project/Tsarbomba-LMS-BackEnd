package com.wanted.codebombalms.domain.user.infrastructure.persistence;

import com.wanted.codebombalms.domain.user.domain.model.User;
import com.wanted.codebombalms.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByUserId(Long userId) {
        return springDataUserRepository.findByUserId(userId)
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return springDataUserRepository.existsByNickname(nickname);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity;

        if (user.getUserId() == null) {
            // 신규 저장
            entity = UserJpaEntity.from(user);
        } else {
            // 업데이트 — 기존 엔티티 찾아서 상태 반영 (JPA 변경감지 활용)
            entity = springDataUserRepository.findByUserId(user.getUserId())
                    .orElseGet(() -> UserJpaEntity.from(user));
            entity.applyDomain(user);
        }

        return springDataUserRepository.save(entity).toDomain();
    }
}