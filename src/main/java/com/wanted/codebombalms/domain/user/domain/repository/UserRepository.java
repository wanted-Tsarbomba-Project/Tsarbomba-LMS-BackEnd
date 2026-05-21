package com.wanted.codebombalms.domain.user.domain.repository;

import com.wanted.codebombalms.domain.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);
}