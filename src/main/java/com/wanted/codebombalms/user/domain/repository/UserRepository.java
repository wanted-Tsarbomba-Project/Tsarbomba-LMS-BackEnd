package com.wanted.codebombalms.user.domain.repository;

import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    List<User> findByUserIds(List<Long> userIds);

    Optional<User> findByNameAndPhone(String name, String phone);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);
    User saveAndFlush(User user);

    List<User> findAllByRole(UserRole role, int page, int size);

    List<User> findAllByRole(UserRole role);

    long countByRole(UserRole role);
}
