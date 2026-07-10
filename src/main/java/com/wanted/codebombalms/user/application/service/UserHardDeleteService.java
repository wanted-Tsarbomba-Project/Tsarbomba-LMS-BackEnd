package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.port.UserHardDeletePort;
import com.wanted.codebombalms.user.application.usecase.HardDeleteUsersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHardDeleteService implements HardDeleteUsersUseCase {

    private final UserHardDeletePort userHardDeletePort;
    private final UserHardDeleteProcessor userHardDeleteProcessor;

    @Override
    @Transactional(readOnly = true)
    public int hardDeleteBefore(LocalDateTime threshold) {
        List<Long> targetUserIds = userHardDeletePort.findDeletedUserIdsBefore(threshold);

        int deletedCount = 0;
        for (Long userId : targetUserIds) {
            if (userHardDeleteProcessor.hardDelete(userId)) {
                deletedCount++;
            }
        }

        log.info("회원 하드 딜리트 완료. 대상={}건, 삭제={}건, 기준시각={}",
                targetUserIds.size(), deletedCount, threshold);
        return deletedCount;
    }
}
