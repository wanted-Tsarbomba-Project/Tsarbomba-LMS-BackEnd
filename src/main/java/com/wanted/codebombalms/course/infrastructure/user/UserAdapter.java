package com.wanted.codebombalms.course.infrastructure.user;

import com.wanted.codebombalms.course.application.port.UserCatalogPort;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("courseUserAdapter")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdapter implements UserCatalogPort {

    private final UserRepository userRepository;

    @Override
    public boolean isActiveOperator(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        return user.getRole() == UserRole.OPERATOR
                && !user.isLocked()
                && !user.isDeleted();
    }
}
