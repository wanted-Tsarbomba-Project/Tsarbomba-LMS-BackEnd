package com.wanted.codebombalms.user.application.usecase;

import java.time.LocalDateTime;

public interface HardDeleteUsersUseCase {

    int hardDeleteBefore(LocalDateTime threshold);
}
