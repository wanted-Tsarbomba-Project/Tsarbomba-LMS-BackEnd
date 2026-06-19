package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByNameAndPhoneAndDeletedAtIsNull(String name, String phone);

    Optional<UserJpaEntity> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<UserJpaEntity> findAllByRoleOrderByCreatedAtDesc(UserRole role, Pageable pageable);

    List<UserJpaEntity> findAllByRoleOrderByCreatedAtDesc(UserRole role);

    long countByRole(UserRole role);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE u.role = :role
              AND u.deletedAt IS NULL
              AND (
                    :keyword IS NULL
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY u.createdAt DESC, u.userId DESC
            """)
    Page<UserJpaEntity> findActiveByRoleAndKeyword(
            @Param("role") UserRole role,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
