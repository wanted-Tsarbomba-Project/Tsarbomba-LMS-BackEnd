package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.application.service.AuthSessionManager;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.usecase.ChangeStudentLockUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.codebombalms.serviceevent.application.port.ServiceEventRecorder;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeStudentLockService implements ChangeStudentLockUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthSessionManager authSessionManager;

    private final ServiceEventRecorder serviceEventRecorder;

    @Override
    public void changeLock(Long userId, boolean locked) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        if (locked) {
            user.lock();
            refreshTokenRepository.deleteByUserId(userId);
            authSessionManager.close(userId);
        } else {
            user.unlock();
        }

        userRepository.save(user);

        serviceEventRecorder.record(ServiceEventEnvelope.business(
                locked ? ServiceEventType.ACCOUNT_LOCKED : ServiceEventType.ACCOUNT_UNLOCKED,
                userId, null,
                "locked=" + locked));
    }
}
