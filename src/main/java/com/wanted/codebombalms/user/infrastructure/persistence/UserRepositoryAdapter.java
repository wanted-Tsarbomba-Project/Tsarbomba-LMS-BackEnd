package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public Optional<User> findByNameAndPhone(String name, String phone) {
        return springDataUserRepository.findByNameAndPhoneAndDeletedAtIsNull(name, phone)
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
        return springDataUserRepository.save(toEntity(user)).toDomain();
    }

    @Override
    public User saveAndFlush(User user) {
        return springDataUserRepository.saveAndFlush(toEntity(user)).toDomain();
    }

    private UserJpaEntity toEntity(User user) {
        if (user.getUserId() == null) {
            // 신규 저장
            return UserJpaEntity.from(user);
        }
        // 업데이트 — 기존 엔티티 찾아서 상태 반영 (JPA 변경감지 활용)
        UserJpaEntity entity = springDataUserRepository.findByUserId(user.getUserId())
                .orElseGet(() -> UserJpaEntity.from(user));
        entity.applyDomain(user);
        return entity;
    }

    @Override
    public List<User> findAllByRole(UserRole role, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return springDataUserRepository.findAllByRoleOrderByCreatedAtDesc(role, pageable).stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<User> findAllByRole(UserRole role) {
        return springDataUserRepository.findAllByRoleOrderByCreatedAtDesc(role).stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByRole(UserRole role) {
        return springDataUserRepository.countByRole(role);
    }
}
