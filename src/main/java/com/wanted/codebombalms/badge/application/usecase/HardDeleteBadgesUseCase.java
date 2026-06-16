package com.wanted.codebombalms.badge.application.usecase;

import java.time.LocalDateTime;

public interface HardDeleteBadgesUseCase {

    int hardDeleteBefore(LocalDateTime threshold);
}
