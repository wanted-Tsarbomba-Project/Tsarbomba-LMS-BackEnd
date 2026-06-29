package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public interface UserOperationQueryUseCase {

    List<UserOperationView> findStudents(int page, int size);

    record UserOperationView(
            Long userId,
            String name,
            String nickname,
            String email,
            UserRole role,
            boolean locked,
            LocalDateTime createdAt
    ) {
    }
}
