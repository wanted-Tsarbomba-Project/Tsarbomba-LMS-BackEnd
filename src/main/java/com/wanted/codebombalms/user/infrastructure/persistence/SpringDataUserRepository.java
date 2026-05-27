package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<UserJpaEntity> findAllByRoleOrderByCreatedAtDesc(UserRole role, Pageable pageable);

    long countByRole(UserRole role);
}
