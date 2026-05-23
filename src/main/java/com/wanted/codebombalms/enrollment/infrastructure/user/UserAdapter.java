package com.wanted.codebombalms.enrollment.infrastructure.user;

import com.wanted.codebombalms.enrollment.application.port.UserCatalogPort;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("enrollmentUserAdapter")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdapter implements UserCatalogPort {

    private final UserRepository userRepository;

    @Override
    public boolean isActiveStudent(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        return user.getRole() == UserRole.STUDENT
                && !user.isLocked()
                && !user.isDeleted();
    }
}
