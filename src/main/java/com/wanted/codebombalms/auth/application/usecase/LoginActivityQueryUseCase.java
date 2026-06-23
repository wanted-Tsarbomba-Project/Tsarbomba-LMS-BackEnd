package com.wanted.codebombalms.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LoginActivityQueryUseCase {

    Map<Long, LocalDateTime> findLatestLoginAtByUserIds(List<Long> userIds);
}
