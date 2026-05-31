package com.wanted.codebombalms.user.domain.repository;

import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);

    List<User> findAllByRole(UserRole role, int page, int size);

    List<User> findAllByRole(UserRole role);

    long countByRole(UserRole role);
}
