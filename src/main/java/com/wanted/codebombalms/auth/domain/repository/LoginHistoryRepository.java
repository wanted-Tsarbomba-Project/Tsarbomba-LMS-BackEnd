package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.LoginHistory;

import java.util.Optional;

public interface LoginHistoryRepository {

    LoginHistory save(LoginHistory loginHistory);

    Optional<LoginHistory> findLatestByUserId(Long userId);
}
