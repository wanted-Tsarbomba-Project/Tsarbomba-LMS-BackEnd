package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.port.UserHardDeletePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteProcessor {

    private final UserHardDeletePort userHardDeletePort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean hardDelete(Long userId) {
        try {
            userHardDeletePort.purgeByUserId(userId);
            return true;
        } catch (RuntimeException e) {
            log.atError()
                    .setCause(e)
                    .log("회원 하드 딜리트 실패로 건너뜁니다. userId={}", userId);
            return false;
        }
    }
}
