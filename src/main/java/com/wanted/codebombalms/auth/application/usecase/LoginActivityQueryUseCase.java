package com.wanted.codebombalms.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginActivityQueryUseCase {

    Optional<LocalDateTime> findLatestLoginAt(Long userId);
}
