package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.query.StudentDetail;
import com.wanted.codebombalms.user.application.usecase.GetStudentDetailUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudentDetailService implements GetStudentDetailUseCase {

    private final UserRepository userRepository;

    @Override
    public StudentDetail getStudentDetail(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        return StudentDetail.from(user);
    }
}
