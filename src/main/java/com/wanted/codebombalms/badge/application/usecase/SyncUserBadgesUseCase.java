package com.wanted.codebombalms.badge.application.usecase;

import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;

public interface SyncUserBadgesUseCase {

    BadgeSyncResult sync(Long userId, int totalPoint);
}
